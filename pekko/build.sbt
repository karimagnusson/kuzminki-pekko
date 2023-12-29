scalaVersion := "2.13.12"

name := "kuzminki-ec-pekko"

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
      "org.apache.pekko" %% "pekko-actor" % "1.0.2",
      "org.apache.pekko" %% "pekko-stream" % "1.0.2"
    )
  )

