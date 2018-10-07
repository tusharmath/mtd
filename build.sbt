name := "scala-mtd"
version := "0.1"
scalaVersion := "2.12.6"
fork := true

libraryDependencies ++= Seq(
  "org.http4s"     %% "http4s-dsl"          % "0.18.17",
  "org.http4s"     %% "http4s-blaze-client" % "0.18.17",
  "ch.qos.logback" % "logback-classic"      % "1.2.3",
  "org.scalactic"  %% "scalactic"           % "3.0.5",
  "org.scalatest"  %% "scalatest"           % "3.0.5" % "test"
)
