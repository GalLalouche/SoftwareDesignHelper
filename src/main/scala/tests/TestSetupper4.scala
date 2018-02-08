package tests

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import common.rich.path.{Directory, RichFile, RichFileUtils}

import scala.annotation.tailrec
import scala.sys.process._

private class TestSetupper4(nextSubmission: Submission)(implicit hc: HomeworkConfiguration) {
  def setup(): Unit = {
    assert(hc.parentDir.exists())
    try {
      val tempDirectory = hc.parentDir.addSubDir("unzip_temp")
      unzip(tempDirectory)
      setupCurrent(tempDirectory)
      copyTestFiles()
      runMaven()
      moveToTested()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        val gradesFile = setupGradesFile()
        gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},***,-")
        throw e
    }
  }

  private def unzip(tempDirectory: Directory): Unit = {
    tempDirectory.clear()
    TestSetupper4.unzip(nextSubmission.zipFile, tempDirectory)
    tempDirectory.files.find(_.extension == "zip").foreach(TestSetupper4.unzip(_, tempDirectory))
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
    TestSetupper4.copyMerge(hc.testTemplate, hc.currentDir / hc.testModuleName /)
  }

  private def getMvnCommandForTest(testName: String): Seq[String] = {
    Seq("""C:\dev\apache\apache-maven-3.3.9\bin\mvn.cmd""", s"-Dtest=$testName", "test", "-DfailIfNoTests=false", "-DprintSummary=false")
  }

  /** Returns the number of failed tests. */
  private def outputMvnTest(testName: String, totalTests: Int): Int = {
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    val mvnCommand = getMvnCommandForTest(testName)
    val mvnOutput = Process(mvnCommand, hc.currentDir).lineStream_!.mkString("\n")
    println(mvnOutput)
    val mvnFile = submissionGradesDir.addFile(s"mvn_${testName}_output.txt")
    mvnFile.write(mvnOutput)
    mvnOutput.split("\n").find(_.startsWith(s"Tests run: $totalTests, Failures: ")).map(testsRunOutput => {
      val (total, failures, errors, skipped) = testsRunOutput.split(", ").mapTo(l => {
        def parseNumber(i: Int) = l(i).split(" ").last.toInt
        (parseNumber(0), parseNumber(1), parseNumber(2), parseNumber(3))
      })
      println((total, failures, errors, skipped))
      failures + errors + skipped
    }) getOrElse 10
  }

  private def setupGradesFile(): RichFile = {
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    val gradesFile = submissionGradesDir.addFile("grade.csv")
    gradesFile.clear().appendLine("ID1,ID2,Failing tests,Grade")
  }

  private def runMaven(): Unit = {
    println("Running mvn...")
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    submissionGradesDir.clear()
    val numberOfFailedTests = outputMvnTest("StaffTest", 10)
    val gradesFile = setupGradesFile()
    if (numberOfFailedTests < 10) {
      gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},$numberOfFailedTests,${100 - (10 * numberOfFailedTests)}")
    } else {
      println("Student failed all tests or code failed to compile! no grade for you!")
      gradesFile.appendLine(s"${nextSubmission.id1},${nextSubmission.id2},all,-")
    }

    // TODO move to RichFile
  }

  private def moveToTested(): Unit = {
    RichFileUtils.move(nextSubmission.zipFile, hc.testedDir)
  }
}

object TestSetupper4 {
  private implicit val hc: HomeworkConfiguration = SubConfiguration
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
    new TestSetupper4(picker.chooseNext).setup()
    1 to 10 foreach (_ => println("--------------------"))
    main(args)
  }
}


