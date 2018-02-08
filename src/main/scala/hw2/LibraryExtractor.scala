package hw2

import java.io.File

import common.rich.path.{Directory, RichFile}
import common.rich.collections.RichTraversableOnce._
import tests.TestSetupper2

object LibraryExtractor {
  private val zipDir: Directory = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign2\Setup\Zips""")
  private val libsDir = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign2\Setup\Libraries""")
  private val tempDir = zipDir.parent.addSubDir("temp")
  private def unzip(zipFile: RichFile): Unit = {
    tempDir.clear()
    TestSetupper2.unzip(zipFile, tempDir)
    val libDir: Directory = {
      def findLibDir(dir: Directory): Option[Directory] = dir.dirs.find(_.name == "library")
      findLibDir(tempDir).orElse(findLibDir(tempDir.dirs.single)).get
    }
    val copiedLibrary = libDir.copyTo(libsDir, zipFile.nameWithoutExtension)
    (copiedLibrary \ "target").delete()
  }
  def main(args: Array[String]): Unit = {
    libsDir.clear()
    zipDir.files foreach {f => {
      try {
        unzip(f)
      } catch {
        case e: Exception =>
          println("Failed to extract zip file " + f)
          e.printStackTrace()
          throw e
      }
    }
    }
    tempDir.clear()
  }
}
