import sbt._
import Keys._

val paradiseVersion = "2.1.0"

val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "org.scalamacros",
  version := "1.0.0",
  scalacOptions ++= Seq(),
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.10.5", "2.10.6",
    "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4",
    "2.11.5", "2.11.6", "2.11.7", "2.11.8",
    "2.12.0", "2.12.1", "2.12.2", "2.12.3", "2.12.4", "2.12.8"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
)

lazy val macros = (project in file("macros"))
  .settings(buildSettings: _*)
  .settings(
    Seq(
      libraryDependencies += scalaVersion("org.scala-lang" % "scala-reflect" % _).value,
      libraryDependencies ++= (
        if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % paradiseVersion) else Nil)
    )
  )

lazy val root = (project in file("."))
  .settings(buildSettings: _*)
  .dependsOn(macros)
