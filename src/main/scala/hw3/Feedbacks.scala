package hw3

import java.io.{File, FileInputStream}

import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import common.rich.path.{Directory, RichFile}
import org.apache.poi.ss.usermodel.{Cell, WorkbookFactory}

import scala.sys.process._
import scala.util.Try

object Feedbacks {
  def unzip(file: RichFile, tempDir: Directory) = {
    println(s"unzipping $file to $tempDir")
    Seq("""c:\Program Files\7-Zip\7z.exe""", "x", s"-o${tempDir.path}", "-y", file.path).!!
  }
  private val feedbackDir = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign3\Testing\feedbacks""")
  private val submissionsDir = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign3\Testing\Submissions\Ball of Mud""")
  private val tempDir = feedbackDir addSubDir "temp_zips"
  private def extractFeedbacks(f: File): Unit = {
    assert(tempDir.exists())
    if (tempDir.deepPaths.nonEmpty)
      tempDir.clear()
    unzip(f, tempDir)
    try {
      val feedbackFile = tempDir.deepFiles.filter(_.extension |> Set("xls", "xlsx")).single
      feedbackFile.copyTo(feedbackDir, f.name + "-feedback." + feedbackFile.extension)
    } catch {
      case _: NoSuchElementException =>
        println(s"File <$f> doesn't contain feedback.xls")
      case e: UnsupportedOperationException =>
        println(s"File <$f> contains multiple feedback.xls")
        throw e
    }
  }

  case class ApiIndex(index: Int) {
    require(index >= 0 && index <= 30)
  }
  private case class Index private(id1: String, id2: String, index: ApiIndex) {
    require(id1 < id2)
    require(id1.matches("\\d{9}"))
    require(id2.matches("\\d{9}"))
  }
  private object Index {
    def from(id1: String, id2: String, index: ApiIndex): Index =
      if (id1 < id2) new Index(id1, id2, index) else new Index(id2, id1, index)
  }
  private case class Feedback(feedbacks: Map[ApiIndex, Option[String]], chosenApi: ApiIndex) {
    require(feedbacks.contains(chosenApi))
  }
  private def parseFeedback(feedbackFile: File): Feedback = {
    println("Parsing file " + feedbackFile)
    try {
      // Sure, let's have runtime typesafe cells!
      def getCellIntValue(c: Cell): Int = Try(c.getNumericCellValue.toInt).getOrElse(c.getStringCellValue.toInt)
      val sheet = WorkbookFactory create new FileInputStream(feedbackFile) getSheetAt 0
      val chosenApi = sheet.getRow(1).getCell(1) |> getCellIntValue |> ApiIndex
      val map = (1 to 7).map(sheet.getRow).map(r => {
        val apiIndex = ApiIndex(r.getCell(1) |> getCellIntValue)
        val desc = r.getCell(2).opt.map(_.getStringCellValue).filter(_.nonEmpty)
        apiIndex -> desc
      }).toMap

      Feedback(map, chosenApi)
    }
  }
  private def getIndexes: Seq[Index] = {
    val indexFile: File = new File("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign2\Setup\Libraries\index.txt""")
    def parseIndex(l: String): Index = l.split("[\\-, ]").mapTo(s => Index.from(s(0), s(1), ApiIndex(s(2).toInt)))
    indexFile.lines.map(l => parseIndex(l))
  }

  def main(args: Array[String]): Unit = {
    //feedbackDir.files.foreach(_.delete())
    //submissionsDir.files.filter(_.extension == "zip").foreach(extractFeedbacks)
    //val names = feedbackDir.files.flatMap(e => e.nameWithoutExtension.mapTo(n => n.split("[-.]")).mapTo(e => List(e(0), e(1))))
    //println(names.sorted.mkString("\n"))
    //
    //val actual = Set("038060109", "200729218", "200940682", "200940690", "201349537", "201510468", "203763073", "203906425", "203934450", "203995121", "204657043", "206159121", "206189383", "206829145", "207071812", "208461871", "209010172", "300816634", "300827565", "300844701", "301248134", "301386900", "301554945", "301690228", "301780086", "302628920", "302765011", "302945761", "305119737", "305320129", "307270215", "310781596", "312250012", "312561913", "313204448", "313413353", "313590937", "314780339", "314973140", "315784785", "315831099", "315835355", "315943829", "316011618", "316086685", "316088376", "316089796", "316159730", "316216779", "316265016", "316331297", "321246720", "321342081", "327154464")
    //val expected = Set("038060109", "200729218", "200940682", "200940690", "201349537", "201510468", "203763073", "203906425", "203934450", "203995121", "204657043", "205841109", "206159121", "206189383", "206829145", "207071812", "208461871", "209010172", "300816634", "300827565", "300844701", "301248134", "301386900", "301554945", "301690228", "301780086", "302628920", "302765011", "302945761", "305119737", "305320129", "307270215", "307857268", "310781596", "312250012", "312421332", "312561913", "313204448", "313413353", "313590937", "313614505", "314780339", "314973140", "315397810", "315784785", "315831099", "315835355", "315943829", "316011618", "316086685", "316088376", "316089796", "316159730", "316216779", "316218452", "316265016", "316331297", "316356039", "318688769", "321246720", "321342081", "327154464")
    //
    //println(expected.diff(actual))
    //val indexMap = getIndexes
    //println(indexMap sortBy (_.index) mkString "\n")
    val indexes = getIndexes.mapBy(_.index)
    val feedbacks = feedbackDir.files.map(parseFeedback)
    feedbacks.flatMap(_.feedbacks).toMultiMap(_._1)(ApiIndex(15)).log(_ mkString "\n")
    val bonus = feedbacks.map(_.chosenApi).frequencies
    bonus.flatMap {
      case (apiIndex, count) =>
        val submitters = indexes(apiIndex)
        List(submitters.id1 -> count, submitters.id2 -> count)
    }.toSeq.sorted.log(_ mkString "\n")
  }
}
