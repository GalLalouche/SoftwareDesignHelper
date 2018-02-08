package tests
import common.rich.path.Directory

object GraderConfiguration extends HomeworkConfiguration {
  override def parentDir: Directory = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign0\Testing""")
  override def moduleName: String = "grades"
}
