scalaVersion := "2.13.12"

name := "kuzminki-ec-akka"

version := "0.9.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

lazy val root = (project in file("."))
  .settings(
    name := "kuzminki-zio",
    libraryDependencies ++= Seq(
      "io.github.karimagnusson" % "kuzminki-ec" % "0.9.4",
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20"
    )
  )

