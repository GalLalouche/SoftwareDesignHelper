package tests

import common.rich.path.Directory

object SubConfiguration extends HomeworkConfiguration {
  override def parentDir: Directory = Directory("""C:\dev\ide\workspaces\SoftwareDesign\2017\SoftwareDesign3\Testing""")
  override def moduleName: String = "sub"
}
