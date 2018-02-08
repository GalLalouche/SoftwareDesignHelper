package tests

import common.rich.RichT._
import common.rich.path.RichFile._

class GradesExtractor(implicit homeworkConfiguration: HomeworkConfiguration) {
  def apply(): Map[String, Int] = {
    val dir = homeworkConfiguration.gradesDir
    val gradeFiles = dir.deepFiles.filter(_.name == "grade.csv")
    gradeFiles.foldLeft(Map[String, Int]())((m, f) => {
      val (id1, id2, grade) = f.lines(1).split(',').mapTo(e => (e(0), e(1), e.last))
      if (grade == "-")
        m
      else {
        try {
          val gradeInt = grade.toInt
          m + (id1 -> gradeInt) + (id2 -> gradeInt)
        } catch {
          case e: Exception =>
            println("Invalid string in file " + f.path)
            throw e
        }
      }
    })
  }
}

object GradesExtractor {
  private implicit val hc = BuyConfiguration
  def main(args: Array[String]): Unit = {
    val $ = new GradesExtractor()
    val gradesString = $.apply().map(e => s"${e._1} ${e._2}").mkString("\n")
    println(gradesString)
    hc.gradesDir.addFile("grades.csv").write(gradesString)
  }
}
