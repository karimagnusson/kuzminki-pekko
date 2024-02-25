
inThisBuild(List(
  organization := "io.github.karimagnusson",
  homepage := Some(url("https://github.com/karimagnusson/kuzminki-ec-stream")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "karimagnusson",
      "Kari Magnusson",
      "kotturinn@gmail.com",
      url("https://github.com/karimagnusson")
    )
  )
))

ThisBuild / version := "0.9.2"
ThisBuild / versionScheme := Some("early-semver")

scalaVersion := "3.3.1"

lazy val scala3 = "3.3.1"
lazy val scala213 = "2.13.12"
lazy val supportedScalaVersions = List(scala213, scala3)

lazy val root = (project in file("."))
  .aggregate(kuzminkiEcPekko)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val kuzminkiEcPekko = (project in file("kuzminki-ec-pekko"))
  .settings(
    name := "kuzminki-ec-pekko",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "io.github.karimagnusson" %% "kuzminki-ec" % "0.9.5-RC3",
      "org.apache.pekko" %% "pekko-actor" % "1.0.2",
      "org.apache.pekko" %% "pekko-stream" % "1.0.2",
    ),
    Compile / scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-feature",
      "-language:higherKinds",
      "-language:existentials",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _))  => Seq("-rewrite")
        case _             => Seq("-Xlint")
      }
    },
  )