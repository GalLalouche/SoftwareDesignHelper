package hw3

import gen.Randomable
import hw2.Order.OrderRandomable

import scala.annotation.tailrec
import scala.util.Random

object Utils {
  def shortId(r: Random): String = r.alphanumeric.take(3).mkString("").toLowerCase
}
import Utils._
case class Journal(id: String, price: Int)
object Journal {
  implicit object ProductRandomable extends Randomable[Journal] {
    override def generate(r: Random): Journal =
      Journal(id = shortId(r), price = r.nextInt(1000000))
  }
}

case class Subscription(userId: String, journalId: String)
object Subscription {
  implicit object SubscriptionRandomable extends Randomable[Subscription] {
    override def generate(r: Random): Subscription =
      Subscription(userId = shortId(r), journalId = shortId(r))
  }
}
case class Cancel(userId: String, journalId: String)
object Cancel {
  implicit object CancelRandomable extends Randomable[Cancel] {
    override def generate(r: Random): Cancel =
      Cancel(userId = shortId(r), journalId = shortId(r))
  }
}
