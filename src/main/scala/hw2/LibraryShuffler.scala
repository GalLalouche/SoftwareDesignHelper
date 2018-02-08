package hw2

import java.io.File

import common.rich.collections.RichSeq._
import common.rich.path.{Directory, RichFile, RichFileUtils}

import scala.annotation.tailrec
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{CellStyle, Font}

object LibraryShuffler {
  private val numberOfPermutations = 7
  case class Submission private(id1: String, id2: String, dir: Directory, index: Int)
  object Submission {
    private var index = 0
    private def apply(d: Directory): Submission = {
      assert(d.name.matches("""\d+-\d+"""))
      val split = d.name.split("-")
      val $ = Submission(split(0), split(1), d, index)
      index += 1
      $
    }

    def submissions(parentDir: Directory): Seq[Submission] = {
      println("Parsing submission dir")
      parentDir.dirs.map(this.apply)
    }
  }

  case class LibraryChoice(ls: Seq[Submission]) {
    require(ls.size == numberOfPermutations)
  }

  private def verifyNoDupes(parentDir: Directory): Unit = {
    def orderedName(d: Directory) = {
      assert(d.name.matches("""\d+-\d+"""))
      val split = d.name.split("-")
      val (id1, id2) = split(0) -> split(1)
      if (id1 > id2) s"$id1-$id2" else s"$id2-$id1"
    }
    println("Verifying no dupes")
    assert(parentDir.dirs.map(orderedName).allUnique)
  }

  private def createDisjointShuffles[T](s: Seq[T]): Seq[Seq[T]] = {
    def areDisjoint(s1: Seq[T], s2: Seq[T]) =
      s1 zip s2 forall (e => e._1 != e._2)
    @tailrec
    def aux(result: Seq[Seq[T]]): Seq[Seq[T]] =
      if (result.size == numberOfPermutations + 1)
        result.dropRight(1)
      else {
        val shuffle = s.shuffle
        aux(if (result.forall(areDisjoint(shuffle, _))) shuffle :: result else result)
      }
    println("Creating permutations")
    aux(List(s))
  }

  private def createLibraryChoices(parentDir: Directory): Map[Submission, LibraryChoice] = {
    println("dispersing libs")
    val submissions = Submission submissions parentDir
    createDisjointShuffles(submissions)
        .transpose
        .zip(submissions)
        .map(_.swap)
        .toMap
        .mapValues(LibraryChoice)
  }

  private def setupOutputDir(outputDir: Directory, map: Map[Submission, LibraryChoice]): Unit = {
    outputDir.clear()
    for ((s, chosenLibs) <- map) {
      val submitterDir = outputDir.addSubDir(s"${s.id1}-${s.id2}")
      chosenLibs.ls.foreach(l => l.dir.copyTo(submitterDir, f"${l.index}%02d"))
    }
  }

  def writeIndices(output: Directory, map: Map[Submission, LibraryChoice]): Unit =
    output
        .addFile("index.txt")
        .clear()
        .appendLine("ids,index,received indices")
        .write(map
            .keys
            .toSeq.sortBy(_.index)
            .map(e => s"${e.id1}-${e.id2}, ${e.index},${map(e).ls.map(_.index).sorted.mkString("[", ";", "]")}")
            .mkString("\n"))

  def main(args: Array[String]): Unit = {
//        LibraryExtractor.main(args)
        val libDir: Directory = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign2\Setup\Libraries""")
        val output: Directory = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign2\Setup\Output""")
        verifyNoDupes(libDir)
        val map = createLibraryChoices(libDir)
        setupOutputDir(output, map)
        writeIndices(output, map)
  }
}
