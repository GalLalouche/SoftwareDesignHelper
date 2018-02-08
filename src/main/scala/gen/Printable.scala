package gen

trait Printable[T] {
  def stringify(t: T): String
}
