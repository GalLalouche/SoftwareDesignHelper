package tests

import java.awt.Desktop
import java.io.File
import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._
import java.nio.file.{Files, StandardCopyOption}

import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import common.rich.path.{Directory, RichFile, RichFileUtils}

import scala.annotation.tailrec
import scala.sys.process._
import scala.util.Try

private class TestSetupper2(nextSubmission: Submission)(implicit hc: HomeworkConfiguration) {
  def setup(): Unit = {
    assert(hc.parentDir.exists())
    try {
      val tempDirectory = hc.parentDir.addSubDir("unzip_temp")
      unzip(tempDirectory)
      setupCurrent(tempDirectory)
      copyTestFiles()
      //    verifyExample()
      //    if (verifyExample())
      runMaven()
      //    else
      //    failedExampleTest()
      moveToTested()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        val gradesFile = setupGradesFile()
        gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},-,-,-,***,-")
        Desktop.getDesktop.open(gradesFile)
    }
  }

  private def unzip(tempDirectory: Directory): Unit = {
    tempDirectory.clear()
    TestSetupper2.unzip(nextSubmission.zipFile, tempDirectory)
    tempDirectory.files.find(_.extension == "zip").foreach(TestSetupper2.unzip(_, tempDirectory))
  }

  private def setupCurrent(tempDirectory: Directory): Unit = {
    println("Setting up 'current' directory")
    hc.currentDir.clear()
    @tailrec
    def findBaseDir(directory: Directory): Directory = {
      if (directory.files.exists(_.name.toLowerCase == "pom.xml"))
        directory
      else
        findBaseDir(directory.dirs.single)
    }
    RichFileUtils.moveContents(findBaseDir(tempDirectory), hc.currentDir)
  }

  private def copyTestFiles(): Unit = {
    println("Copying test files")
    TestSetupper2.copyMerge(hc.testTemplate, hc.currentDir / hc.testModuleName /)
  }

  private def getMvnCommandForTest(testName: String): Seq[String] = {
    Seq("""C:\dev\apache\apache-maven-3.3.9\bin\mvn.cmd""", s"-Dtest=$testName", "test", "-DfailIfNoTests=false", "-DprintSummary=false")
  }

  /** Returns true if the example test passed. */
  private def passedExample(): Boolean = {
    val $ = Try(getMvnCommandForTest("ExampleTest").!!).isSuccess
    if (!$) {
      outputMvnTest("ExampleTest", 1)
    }
    $
  }

  /** Returns true if not all tests failed. */
  private def outputMvnTest(testName: String, totalTests: Int): Boolean = {
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    val mvnCommand = getMvnCommandForTest(testName)
    val mvnOutput = Process(mvnCommand, hc.currentDir).lineStream_!.mkString("\n")
    println(mvnOutput)
    val mvnFile = submissionGradesDir.addFile(s"mvn_${testName}_output.txt")
    mvnFile.write(mvnOutput)
    mvnOutput.split("\n").find(_.startsWith(s"Tests run: $totalTests, Failures: ")).exists(testsRunOutput => {
      val (total, failures, errors, skipped) = testsRunOutput.split(", ").mapTo(l => {
        def parseNumber(i: Int) = l(i).split(" ").last.toInt
        (parseNumber(0), parseNumber(1), parseNumber(2), parseNumber(3))
      })
      println((total, failures, errors, skipped))
      total > failures + errors + skipped
    })
  }

  private def setupGradesFile(): RichFile = {
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    val gradesFile = submissionGradesDir.addFile("grade.csv")
    gradesFile.clear().appendLine("ID1,ID2,Failing tests,Used Guice,Wrote tests,Other,Grade")
  }

  private def runMaven(): Unit = {
    println("Running mvn...")
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    submissionGradesDir.clear()
    val mainMvnPassedSomeTests = outputMvnTest("StaffTest", 10)
    val gradesFile = setupGradesFile()
    if (mainMvnPassedSomeTests) {
      gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},")
    } else {
      println("Student failed all tests or code failed to compile! no grade for you!")
      println("Running example test for extra humiliation")
      if (passedExample())
        gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},all,-,-,-,-")
      else
        gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},all + example!,-,-,-,-")
    }

    // TODO move to RichFile
    Desktop.getDesktop.open(gradesFile)
  }

  private def moveToTested(): Unit = {
    RichFileUtils.move(nextSubmission.zipFile, hc.testedDir)
  }
}

object TestSetupper2 {
  private implicit val hc: HomeworkConfiguration = BookConfiguration
  private val picker = new SubmissionsPicker

  // TODO create a zipped file class? hmmm. Maybe just add to RichOs?
  def unzip(file: RichFile, tempDir: Directory) = {
    println(s"unzipping $file to $tempDir")
    Seq("""c:\Program Files\7-Zip\7z.exe""", "x", s"-o${tempDir.path}", "-y", file.path).!!
  }

  private def copyMerge(srcDir: Directory, dstDir: Directory): Unit = {
    for (f <- srcDir.deepFiles) {
      val relativePath = f.path.replaceAll(srcDir.path, "")
      val dstPath = new File(dstDir.path + relativePath);
      {
        val parentFile = dstPath.getParentFile
        if (!parentFile.exists) {
          parentFile.mkdirs()
        }
      }
      Files.copy(f.toPath, dstPath.toPath, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  def main(args: Array[String]): Unit = {
    new TestSetupper2(picker.chooseNext).setup()
  }
}
