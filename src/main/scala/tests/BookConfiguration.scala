package tests

import common.rich.path.Directory

object BookConfiguration extends HomeworkConfiguration {
  override def parentDir: Directory = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign1\Testing""")
  override def moduleName: String = "book"
}
