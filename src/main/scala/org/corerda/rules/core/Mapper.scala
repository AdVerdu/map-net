package org.corerda.rules.core

import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.yaml._
import io.circe.{Decoder, Json}
import org.corerda.entities._

import scala.util.Try

// YAML Mapper
object Mapper {

  /** Decodes a YAML string into a graph of nodes.
    * Returns an Either[Throwable, Map[String, Node[T]]] for functional error handling.
    */
  def fromString[T](payload: String)(implicit taskDecoder: Decoder[Node[T]]): Either[Throwable, Map[String, Node[T]]] = {
    for {
      json <- parser.parse(payload).leftMap(pf => pf: Throwable)
      plan <- json.as[Plan[T]].leftMap(e => e: Throwable)
    } yield plan.tasks
  }
}
