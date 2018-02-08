package tests

import common.rich.path.Directory

trait HomeworkConfiguration {
  def moduleName: String

  def testModuleName: String = s"$moduleName-test"

  def testTemplate: Directory = parentDir / testModuleName /

  def testedDir: Directory = (parentDir / "Submissions" /) addSubDir "tested"

  def gradesDir: Directory = parentDir addSubDir "grades"

  def currentDir: Directory = parentDir addSubDir "current"

  def untestedDir: Directory = parentDir / "Submissions" / "untested" /

  def parentDir: Directory
}
