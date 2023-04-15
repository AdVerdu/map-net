package org.corerda.rules.core

import io.circe.generic.auto._
import io.circe.yaml._
import io.circe.{Decoder, Json, ParsingFailure}

import scala.util.{Failure, Success}
import org.corerda.entities._

// YAML Mapper
object Mapper {
  def fromString[T](payload: String)(implicit taskDecoder: Decoder[Node[T]]): Map[String, Node[T]] = {
    val yaml: Either[ParsingFailure, Json] = parser.parse(payload)
    val plan: Plan[T] =
      yaml.flatMap(_.as[Plan[T]]).toTry match {
      case Success(value) => value
      case Failure(e) => throw new Exception(e)
    }
    plan.tasks
  }
}
