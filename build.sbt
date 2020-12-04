import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / organization := "com.gomezgimenez"

lazy val `g-code-calibrate` = (project in file("."))
  .settings(
    mainClass in (Compile, run) := Some("Main"),
    mainClass in assembly := Some("Main"),
    libraryDependencies += scalaTest % Test,
  )
