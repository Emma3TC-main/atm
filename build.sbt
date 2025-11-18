name := "atm-simulator"
version := "0.1"

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.typelevel" %% "cats-core" % "2.12.0",
  "org.http4s" %% "http4s-ember-server" % "0.23.27",
  "org.http4s" %% "http4s-ember-client" % "0.23.27",
  "org.http4s" %% "http4s-dsl" % "0.23.27",
  "org.http4s" %% "http4s-circe" % "0.23.27",
  "io.circe" %% "circe-generic" % "0.14.7",
  "io.circe" %% "circe-parser" % "0.14.7",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4",
  "com.typesafe" % "config" % "1.4.3",
  "org.postgresql" % "postgresql" % "42.7.3",
  "org.slf4j" % "slf4j-simple" % "2.0.13",
  "com.comcast" %% "ip4s-core" % "3.6.0",
  "org.mindrot" % "jbcrypt" % "0.4"
)

dependencyOverrides += "com.comcast" %% "ip4s-core" % "3.6.0"

Compile / run / fork := true

// CONFIGURACIÓN DE ASSEMBLY
assembly / mainClass := Some("com.atm.Main")
assembly / assemblyJarName := "atm.jar"


import sbtassembly.AssemblyPlugin.autoImport._

assembly / mainClass := Some("com.atm.Main")
assembly / assemblyJarName := "atm.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("META-INF", "versions", _*) => MergeStrategy.discard
  case "module-info.class" => MergeStrategy.discard
  case x => MergeStrategy.first
}
