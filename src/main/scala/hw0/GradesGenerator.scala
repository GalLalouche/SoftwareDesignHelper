package hw0

import java.io.{File, PrintStream}

import gen.Generator

import scala.util.Random

object GradesGenerator {
  private val random = new Random(0)
  import Grade._
  def main(args: Array[String]): Unit = {
    val generator: String = Generator.generateStrings(random).take(1000000).mkString("\n")
    val ps = new PrintStream(
      new File("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign0\Testing\current\grades-test\src\test\resources\il\ac\technion\cs\sd\app\large"""))
    ps.append(generator)
  }
}
