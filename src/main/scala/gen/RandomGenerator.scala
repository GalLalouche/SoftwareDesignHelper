package gen

@FunctionalInterface
trait RandomGenerator[T] {
  def randoms(): Stream[T] = Stream continually random
  def random(): T
  // TODO replace with scala 2.12
  def map[S](f: T => S): RandomGenerator[S] = new RandomGenerator[S] {
    override def random(): S = f(RandomGenerator.this.random())
  }
  def flatMap[S](f: T => RandomGenerator[S]): RandomGenerator[S] = new RandomGenerator[S] {
    override def random(): S = f(RandomGenerator.this.random()).random()
  }
}
