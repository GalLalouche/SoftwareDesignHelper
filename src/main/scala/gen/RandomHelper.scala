package gen

import scala.util.Random

class RandomHelper(r: Random) {
  def alphaNumeric(): Stream[Char] = r.alphanumeric
  def alphaNumeric(length: Int): String = alphaNumeric().take(length).mkString("")
  def alphaNumericOfMaxLength(maxLength: Int): String = alphaNumeric(r.nextInt(maxLength) + 1)
}
