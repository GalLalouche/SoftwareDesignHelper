package tests

import java.awt.Desktop

import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import common.rich.path.{Directory, RichFile, RichFileUtils}

import scala.annotation.tailrec
import scala.sys.process._

class TestSetupper(nextSubmission: Submission)(implicit hc: HomeworkConfiguration) {
  def setup(): Unit = {
    assert(hc.parentDir.exists())
    val tempDirectory = hc.parentDir.addSubDir("unzip_temp")
    unzip(tempDirectory)
    setupCurrent(tempDirectory)
    runMaven()
    moveToTested()
  }

  private def unzip(tempDirectory: Directory): Unit = {
    tempDirectory.clear()
    TestSetupper.unzip(nextSubmission.zipFile, tempDirectory)
    tempDirectory.files.find(_.extension == "zip").foreach(TestSetupper.unzip(_, tempDirectory))
  }

  private def setupCurrent(tempDirectory: Directory): Unit = {
    hc.currentDir.clear()
    @tailrec
    def findBaseDir(directory: Directory): Directory = {
      if (directory.files.exists(_.name.toLowerCase == "pom.xml"))
        directory
      else
        findBaseDir(directory.dirs.single)
    }
    RichFileUtils.moveContents(findBaseDir(tempDirectory), hc.currentDir)
    val currentTestDir = hc.currentDir addSubDir hc.testModuleName
    currentTestDir.deleteAll()
    assert(!currentTestDir.exists)
    hc.testTemplate.copyTo(currentTestDir)
  }

  private def moveToTested(): Unit = {
    RichFileUtils.move(nextSubmission.zipFile, hc.testedDir)
  }

  private def runMaven(): Unit = {
    val mvnCommand = Seq("""C:\dev\apache\apache-maven-3.3.9\bin\mvn.cmd""", "-Dtest=StaffTest", "test", "-DfailIfNoTests=false", "-DprintSummary=false")
    val mvnOutput = Process(mvnCommand, hc.currentDir).lineStream_!.mkString("\n")
    println(mvnOutput)
    val submissionGradesDir = hc.gradesDir.addSubDir(s"${nextSubmission.id1}-${nextSubmission.id2}")
    val mvnFile = submissionGradesDir.addFile("mvn_output.txt")
    mvnFile.write(mvnOutput)
    val gradesFile = submissionGradesDir.addFile("grade.csv")

    gradesFile.clear()
        .appendLine("ID1,ID2,Failing tests,Uses Mockito,Dry part,Other,Grade")
        .appendLine(s"${nextSubmission.id1},${nextSubmission.id2}")
    // TODO move to RichFile
    Desktop.getDesktop.open(gradesFile)
  }
}

object TestSetupper {
  private implicit val hc: HomeworkConfiguration = GraderConfiguration
  private val picker = new SubmissionsPicker

  // TODO create a zipped file class? hmmm
  private def unzip(file: RichFile, tempDir: Directory) = {
    println(s"unzipping $file to $tempDir")
    Seq("""c:\Program Files\7-Zip\7z.exe""", "x", s"-o${tempDir.path}", "-y", file.path).!!
  }

  def main(args: Array[String]): Unit = {
    new TestSetupper(picker.chooseNext).setup()
  }
}
