package org.corerda

import io.circe.Decoder
import org.corerda.entities.Node
import org.corerda.rules.core.Mapper._
import org.corerda.rules.core.TreeOps._
import org.corerda.service.provider.FileProvider
import org.corerda.service.types.IntegerImpl

object UnSafeRunner extends App {
  type set = IntegerImpl.myType

  val planCfg = FileProvider.fromPath("src/test/resources/playground/plans/intGraph.yaml")
  implicit val decoder: Decoder[Node[set]] = IntegerImpl.taskDecoder

  val result = for {
    graph <- fromString[set](planCfg)
    results <- runAST(graph)
  } yield results

  result match {
    case Right(results) =>
      results.foreach(expr => println(s"Result: ${expr.eval.value}"))
    case Left(e) =>
      println(s"Error: ${e.getMessage}")
      e.printStackTrace()
  }
}
