package org.corerda.playground

import io.circe.ParsingFailure
import io.circe.generic.auto._
import io.circe.yaml._
import io.circe.Json

object ReadYaml extends App {
  case class Plan(id: Int, name: String, env: String, version: String, task: Map[String, Task])
  case class Task(`type`: String, from: String, config: Config)
  case class Config(size: Option[Int], tag: Option[String], transformer: Option[List[String]])

  val source = scala.io.Source.fromFile("src/test/resources/playground/plans/intGraph.yaml")
  val yaml: Either[ParsingFailure, Json] = parser.parse(source.mkString)
  val plan: Either[io.circe.Error, Plan] = yaml.flatMap(_.as[Plan])

  source.close()
  println(plan)
}
