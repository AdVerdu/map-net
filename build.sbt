organization := "org.corerda"
name := "MapNet"
version := "0.1.1-SNAPSHOT"

scalaVersion := "2.13.10"

val circeVersion = "0.14.5"
val circeYamlVersion = "0.14.2"
val scalatestVersion = "3.2.10"
val catsVersion = "2.9.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-yaml" % circeYamlVersion,
  // logging
  "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.20.0"
)