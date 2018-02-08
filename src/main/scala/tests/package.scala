import java.io.File
package object tests {
  type BetterFile = better.files.File
  def toBetterFile(f: File): BetterFile = better.files.File.apply(f.toPath)
}
