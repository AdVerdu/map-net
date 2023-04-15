package org.corerda

import org.corerda.rules.core.Job._
import org.corerda.rules.core.Mapper._
import org.corerda.rules.core.TreeOps._
import org.corerda.service.provider.FileProvider
import org.corerda.service.types.IntegerImpl

object UnSafeRunner extends App {
  type set = IntegerImpl.myType

  val planCfg = FileProvider.fromPath("src/test/resources/playground/plans/intGraph.yaml")
  implicit val decoder = IntegerImpl.taskDecoder
  val graph = fromString[set](planCfg)
  val treeGraph = toTree(graph)

  treeGraph.map(foldTree)
}
