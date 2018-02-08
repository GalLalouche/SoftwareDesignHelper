package gen

import scala.util.Random

trait Randomable[T] {
  def generate(r: Random): T
  def randomGenerator(r: Random): RandomGenerator[T] = new RandomGenerator[T] {
    override def random(): T = generate(r)
  }
}
