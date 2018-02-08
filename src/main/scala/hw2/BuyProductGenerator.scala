package hw2

import gen.{Generator, Printable}
import common.rich.RichT._
import common.rich.collections.RichSeq._
import common.rich.path.Directory

import scala.util.Random

private object BuyProductGenerator {
  implicit object ProductJsonPrintable extends Printable[Product] {
    override def stringify(p: Product): String = s"""{"type": "product", "id": "${p.id}", "price": ${p.price}}"""
  }
  implicit object ChainJsonPrintable extends Printable[OrderChain] {
    override def stringify(c: OrderChain): String = {
      val order = c.order.mapTo(o =>
        s"""{"type": "order", "order-id": "${o.orderId}", "user-id": "${o.userId}", "amount": ${o.amount}, "product-id": "${o.productId}"}""")
      val modificationsString: Seq[String] = c.modifications.map(m =>
        s"""{"type": "modify-order", "order-id": "${m.orderId}", "amount": ${m.newAmount}}""")
      val cancelString: Seq[String] = c.cancel.toList.map(c => s"""{"type": "cancel-order", "order-id": "${c.orderId}"}""")
      val $ = order :: (modificationsString.toList ::: cancelString.toList)
      $.mkString("\n")
    }
  }
  object ProductXmlPrintable extends Printable[Product] {
    override def stringify(p: Product): String =
      s"""<Product><id>${p.id}</id><price>${p.price}</price></Product>"""
  }
  object ChainXmlPrintable extends Printable[OrderChain] {
    override def stringify(c: OrderChain): String = {
      val order = c.order.mapTo(o =>
        s"""<Order><user-id>${o.userId}</user-id><order-id>${o.orderId}</order-id><product-id>${o.productId}</product-id><amount>${o.amount}</amount></Order>""".stripMargin)
      val modificationsString: Seq[String] = c.modifications.map(m =>
        s"""<ModifyOrder><order-id>${m.orderId}</order-id><new-amount>${m.newAmount}</new-amount></ModifyOrder>""")
      val cancelString: Seq[String] = c.cancel.toList
          .map(c => s"""<CancelOrder><order-id>${c.orderId}</order-id></CancelOrder>""")
      val $ = order :: (modificationsString.toList ::: cancelString.toList)
      $.mkString("\n")
    }
  }
  val r = new Random
  private def randomInterlacing[T](xs: Seq[T], ys: Seq[T]): Seq[T] = {
    def aux(result: List[T], x1: List[T], x1Length: Int, x2: List[T], x2Length: Int): List[T] = {
      if (x1Length == 0 || x2Length == 0)
        x1.reverse ::: x2.reverse ::: result
      else if (r.nextDouble() < x1Length.toDouble / (x2Length + x1Length))
        aux(x1.head :: result, x1.tail, x1.length - 1, x2, x2Length)
      else
        aux(x2.head :: result, x1, x1Length, x2.tail, x2Length - 1)
    }
    aux(Nil, xs.toList, xs.length, ys.toList, ys.length).reverse
  }
  def main(args: Array[String]): Unit = {
    val products: Seq[String] = Generator.generateStrings[Product](r).take(100000).shuffle
    val orders: Seq[String] = Generator.generateStrings[OrderChain](r).take(100000).shuffle.flatMap(_.split("\n"))
    val allStrings = randomInterlacing(products, orders)
    val dir =
      Directory("""c:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign2\Testing\current\buy-test\src\test\resources\il\ac\technion\cs\sd\buy\test""")
    val str = "<Root>\n" + (allStrings filter (_.nonEmpty) mkString "\n") + "\n</Root>"
//    println(str)
    dir.addFile("large.json").clear().write(str)
  }
}
