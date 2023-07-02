import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "1.2.1"
ThisBuild / organization := "com.gomezgimenez"

lazy val `g-code-utils` = (project in file("."))
  .settings(
    mainClass in (Compile, run) := Some("Main"),
    mainClass in assembly := Some("Main"),
    libraryDependencies ++= List(
      json4s,
      scalaTest % Test,
      "org.apache.commons" % "commons-math" % "2.2"
    )
  )
