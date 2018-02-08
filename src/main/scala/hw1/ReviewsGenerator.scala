package hw1

import java.io.File

import common.rich.path.RichFile._
import gen.{Generator, Printable, RandomHelper, Randomable}
import common.rich.collections.RichTraversableOnce._

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

private object ReviewsGenerator {
  import Reviewer._
  import Book.RandomableBook
  private def fillSet[T](size: Int, f: () => T): Set[T] = {
    val $ = new mutable.HashSet[T]()
    while ($.size < size)
      $ += f()
    $.toSet
  }

  private val maxBooks = 100000
  private val maxReviewers = 100000
  private val maxReviewsPerReviewer = 100
  private val maxReviewsPerBook = 100

  private def generate(r: Random): Set[Reviewer] = {
    import Book.RandomableBook
    val bookSet: Vector[Book] = fillSet(maxBooks, () => RandomableBook.generate(r)).toVector
    val numberOfReviewsPerBook: mutable.Map[Book, Integer] = new mutable.HashMap().withDefaultValue(0)
    val helper = new RandomHelper(r)
    val reviewersIdSet = fillSet(maxReviewers, () => 'r' + helper.alphaNumericOfMaxLength(9))

    @tailrec
    def pickBookWithoutTooManyReviews(): Book = {
      val book = bookSet(r.nextInt(bookSet.size))
      if (numberOfReviewsPerBook(book) >= maxReviewsPerBook)
        pickBookWithoutTooManyReviews()
      else {
        numberOfReviewsPerBook(book) = numberOfReviewsPerBook(book) + 1
        book
      }
    }
    def generateNumberOfReviews(): Int = {
      if (r.nextBoolean())
        1
      else if (r.nextBoolean())
        2
      else if (r.nextBoolean())
        4
      else if (r.nextBoolean())
        8
      else if (r.nextBoolean())
        16
      else if (r.nextBoolean())
        32
      else if (r.nextBoolean())
        64
      else
        100
    }
    reviewersIdSet.map(reviewerId => {
      val numberOfReviews: Int = generateNumberOfReviews()
      val reviews: Seq[Review] = fillSet[Review](generateNumberOfReviews(), () => {
        val book = pickBookWithoutTooManyReviews()
        Review(book = book, score = r.nextInt(10) + 1)
      }).toVector
      Reviewer(id = reviewerId, reviews = reviews)
    })
  }
  private val random = new Random(0)
  def main(args: Array[String]): Unit = {
//    val generated: String = generate(random)
//        .map(implicitly[Printable[Reviewer]].stringify)
//        .mkString("\n")
//        .split('\n')
//        .map("  ".+)
//        .mkString("\n")
//    val root = "<Root>\n" + generated + "\n</Root>"
//
    val output = new File("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign1\Testing\current\book-test\src\test\resources\il\ac\technion\cs\sd\book\test\large.xml""")
    output.createNewFile()
//    output.write(root)
    val freqs = output.lines.filter(_.contains("<Id>")).toSeq.frequencies
    freqs.filter(_._2 == 2).foreach(println)
  }
}
