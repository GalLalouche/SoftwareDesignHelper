package gen

import scala.util.Random

object Generator {
  def generateStrings[T: Randomable : Printable](r: Random): Stream[String] = {
    implicitly[Randomable[T]].randomGenerator(r).randoms().map(implicitly[Printable[T]].stringify)
  }
}
