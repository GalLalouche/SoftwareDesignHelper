package hw1

import gen.{Printable, RandomHelper, Randomable}

import scala.util.Random

private case class Book(id: String) extends AnyVal
private case class Reviewer(id: String, reviews: Seq[Review])
private case class Review(score: Int, book: Book)

private object Book {
  implicit object RandomableBook extends Randomable[Book] {
    override def generate(r: Random): Book = Book('b' + new RandomHelper(r).alphaNumericOfMaxLength(9))
  }
}
private object Review {
  implicit object RandomableReview extends Randomable[Review] {
    override def generate(r: Random): Review = {
      Review(score = r.nextInt(10) + 1, book = Book.RandomableBook.generate(r))
    }
  }
  implicit object PrintableXmlReview extends Printable[Review] {
    override def stringify(r: Review): String =
      s"""<Review>
         |  <Id>${r.book.id}</Id>
         |  <Score>${r.score}</Score>
         |</Review>
      """.stripMargin
  }
}

private object Reviewer {
  implicit object RandomableReviewer extends Randomable[Reviewer] {
    //    override def generator(r: Random): RandomGeneratorImpl[Reviewer] = new Generator(r)
    override def generate(r: Random): Reviewer = {
      val numberOfReviews = r.nextInt(101)
      val helper = new RandomHelper(r)
      Reviewer(id = 'r' + helper.alphaNumericOfMaxLength(9),
        reviews = List.fill(numberOfReviews)(Review.RandomableReview.generate(r)))
    }
  }

  private def lines(s: String): Seq[String] = s split "\n"
  private def unLines(s: Seq[String]): String = s mkString "\n"

  implicit object PrintableXmlReviewer extends Printable[Reviewer] {
    override def stringify(r: Reviewer): String = {
      val opener = s"<Reviewer Id='${r.id}'>\n"
      val reviews = unLines(lines(r.reviews.map(Review.PrintableXmlReview.stringify).mkString("\n")).map("  " + _).filterNot(_.matches("\\s*")))
      val closer = "\n</Reviewer>"
      opener + reviews + closer
    }
  }
}
