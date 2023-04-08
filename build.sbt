version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.10"
name := "DynamicProgramGenerator"

val circeVersion = "0.14.5"
val circeYamlVersion = "0.14.2"
val scalatestVersion = "3.2.15"
val catsVersion = "2.9.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-yaml" % circeYamlVersion
)