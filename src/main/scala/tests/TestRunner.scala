package tests

import common.rich.RichT._
import common.rich.path.RichFile._

class SubmissionsPicker(implicit c: HomeworkConfiguration) {
  def chooseNext: Submission = {
    val nextZip = c.untestedDir.files.filter(_.extension == "zip").head
    val (id1, id2) = nextZip.nameWithoutExtension.split("-").mapTo(e => e(0) -> e(1))
    Submission(id1, id2, nextZip)
  }
}
