package hw2

import gen.Randomable
import hw2.Order.OrderRandomable

import scala.annotation.tailrec
import scala.util.Random

object Utils {
  def shortId(r: Random): String = r.alphanumeric.take(3).mkString("").toLowerCase
}
import Utils._
case class Product(id: String, price: Int)
object Product {
  implicit object ProductRandomable extends Randomable[Product] {
    override def generate(r: Random): Product =
      Product(id = shortId(r), price = r.nextInt(100000))
  }
}

case class Order(orderId: String, userId: String, productId: String, amount: Int)
object Order {
  implicit object OrderRandomable extends Randomable[Order] {
    override def generate(r: Random): Order =
      Order(orderId = shortId(r), userId = shortId(r), productId = shortId(r), amount = r.nextInt(1000))
  }
}
case class ModifyOrder(orderId: String, newAmount: Int)
case class CancelOrder(orderId: String)

case class OrderChain(order: Order, modifications: Seq[ModifyOrder], cancel: Option[CancelOrder])
object OrderChain {
  implicit object RandomableOrderChain extends Randomable[OrderChain] {
    override def generate(r: Random): OrderChain = {
      val order = OrderRandomable.generate(r)
      @tailrec
      def modifyOrders(result: List[ModifyOrder] = Nil): Seq[ModifyOrder] =
        if(r.nextBoolean()) result else modifyOrders(ModifyOrder(order.orderId, r.nextInt(1000)) :: result)
      val modifications = modifyOrders()
      val cancel = if (r.nextBoolean()) None else Some(CancelOrder(order.orderId))
      OrderChain(order, modifications, cancel)
    }
  }
}
