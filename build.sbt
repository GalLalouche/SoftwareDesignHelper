name := "SoftwareDesignHelper"

version := "1.0"

scalaVersion := "2.11.8"

lazy val commonSettings = Seq(
  organization := "org.me",
  version := "0.01",
  scalaVersion := "2.11.8")

resolvers += Resolver.sonatypeRepo("releases")
autoCompilerPlugins := true
lazy val nessie = (project in file("."))
    .settings(commonSettings: _*)
    .settings(
      name := "SoftwareDesignHelper",
      libraryDependencies ++= Seq(
        "org.apache.poi" % "poi" % "3.16",
        "org.apache.poi" % "poi-ooxml" % "3.16",
        "org.me" %% "scalacommon" % "1.0" changing(),
        "com.github.pathikrit" %% "better-files" % "2.17.1"
      ))
