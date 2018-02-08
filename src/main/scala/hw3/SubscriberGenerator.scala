package hw3

import common.rich.collections.RichSeq._
import common.rich.path.Directory
import gen.{Generator, Printable}

import scala.util.Random

private object SubscriberGenerator {
  private val r = new Random

  //  implicit object JournalJsonPrintable extends Printable[Journal] {
  //    override def stringify(j: Journal): String =
  //      s"""{"type": "journal", "journal-id": "${j.id}", "price": ${j.price}}"""
  //  }
  //  implicit object SubscriptionJsonPrintable extends Printable[Subscription] {
  //    override def stringify(s: Subscription): String =
  //      s"""{"type": "subscription", "journal-id": "${s.journalId}", "user-id": "${s.userId}"}"""
  //  }
  //  implicit object CancelJsonPrintable extends Printable[Cancel] {
  //    override def stringify(c: Cancel): String =
  //      s"""{"type": "cancel", "journal-id": "${c.journalId}", "user-id": "${c.userId}"}"""
  //  }
  implicit object JournalCsvPrintable extends Printable[Journal] {
    override def stringify(j: Journal): String = s"""journal,${j.id},${j.price}"""
  }

  implicit object SubscriptionCsvPrintable extends Printable[Subscription] {
    override def stringify(s: Subscription): String = s"""subscriber,${s.userId},${s.journalId}"""
  }

  implicit object CancelCsvPrintable extends Printable[Cancel] {
    override def stringify(s: Cancel): String = s"""cancel,${s.userId},${s.journalId}"""
  }

  //  private def randomInterlacing[T](xs: Seq[T], ys: Seq[T]): Seq[T] = {
  //    def aux(result: List[T], x1: List[T], x1Length: Int, x2: List[T], x2Length: Int): List[T] = {
  //      if (x1Length == 0 || x2Length == 0)
  //        x1.reverse ::: x2.reverse ::: result
  //      else if (r.nextDouble() < x1Length.toDouble / (x2Length + x1Length))
  //        aux(x1.head :: result, x1.tail, x1.length - 1, x2, x2Length)
  //      else
  //        aux(x2.head :: result, x1, x1Length, x2.tail, x2Length - 1)
  //    }
  //    aux(Nil, xs.toList, xs.length, ys.toList, ys.length).reverse
  //  }
  def main(args: Array[String]): Unit = {
    val n = 100000
    val strings = (Generator.generateStrings[Journal](r).take(n) ++
        Generator.generateStrings[Subscription](r).take(n) ++
        Generator.generateStrings[Cancel](r).take(n)).shuffle
    //    val products: Seq[String] = Generator.generateStrings[Product](r).take(100000).shuffle
    //    val orders: Seq[String] = Generator.generateStrings[](r).take(100000).shuffle.flatMap(_.split("\n"))
    //    val allStrings = randomInterlacing(products, orders)
    val dir = Directory("""c:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign3\Testing\current\sub-test\src\test\resources\il\ac\technion\cs\sd\sub\test""")
    //    val str = "<Root>\n" + (allStrings filter (_.nonEmpty) mkString "\n") + "\n</Root>"
    //    println(str)
    //    dir.addFile("large.json").clear().write(strings.mkString("[\n  ", ",\n  ", "\n]"))
    dir.addFile("large.csv").clear().write(strings mkString "\n")
  }
}
