name := "scala-mtd"
version := "0.1"
scalaVersion := "2.12.6"
fork := true

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.18.17",
  "org.http4s" %% "http4s-blaze-client" % "0.18.17",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.8" cross CrossVersion.binary)
libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" => compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) :: Nil
  case _ => Nil
})