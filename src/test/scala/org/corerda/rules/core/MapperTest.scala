package org.corerda.rules.core

import org.corerda.entities._
import org.corerda.service.provider.FileProvider
import org.scalatest.funspec.AnyFunSpec


class MapperTest extends AnyFunSpec {
  val myStrPlan = FileProvider.fromPath("src/test/resources/playground/plans/intGraph.yaml")

  import org.corerda.rules.core.Mapper._
  describe("Mapper Test") {
    it("Should decode a YAML plan into a Map of nodes") {
      import org.corerda.service.types.IntegerImpl
      import org.corerda.service.types.IntegerImpl._

      implicit val _ = IntegerImpl.taskDecoder
      val result = fromString[IntegerImpl.myType](myStrPlan)
      assert(result.isRight)
      val graph = result.toOption.get

      assert(graph.contains("node1"))
      assert(graph("node1").config.isInstanceOf[ReaderCmp])
      assert(graph("node7").predecessor.isInstanceOf[Two[_]])
    }
  }
}
