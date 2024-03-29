package org.corerda

import io.circe.Decoder
import org.corerda.entities.Node
import org.corerda.rules.core.Job._
import org.corerda.rules.core.Mapper._
import org.corerda.rules.core.TreeOps._
import org.corerda.service.provider.FileProvider
import org.corerda.service.types.IntegerImpl

// TODO
//  - delete deprecated Classes
//  - redo Unit tests
object UnSafeRunner extends App {
  type set = IntegerImpl.myType

  val planCfg = FileProvider.fromPath("src/test/resources/playground/plans/intGraph.yaml")
  implicit val decoder: Decoder[Node[set]] = IntegerImpl.taskDecoder
  val graph = fromString[set](planCfg)

  runAST(graph)
}
