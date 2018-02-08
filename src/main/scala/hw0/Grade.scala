package hw0

import gen.{Printable, Randomable}

import scala.util.Random

case class Grade(id: String, grade: Int) {
  require(grade >= 0 && grade <= 100)
  require(id.length <= 10 && id.nonEmpty)
  require(id.forall(_.isDigit))
}

object Grade {
  implicit object RandomableGrade extends Randomable[Grade] {
    override def generate(r: Random): Grade = {
      val idLength = r.nextInt(9) + 1
      Grade(id = Stream.continually(r.nextInt(10)).take(idLength).mkString(""), grade = r.nextInt(101))
    }
  }
  implicit object PrintableGrade extends Printable[Grade] {
    override def stringify(g: Grade): String = s"${g.id},${g.grade}"
  }
}
