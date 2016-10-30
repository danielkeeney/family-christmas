name := """family-christmas"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

//testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q")

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)
