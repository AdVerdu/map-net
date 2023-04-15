package org.corerda.rules.core

import org.corerda.entities._
import org.corerda.service.provider.FileProvider
import org.scalatest.funspec.AnyFunSpec


class MapperTest extends AnyFunSpec {
  val myStrPlan = FileProvider.fromPath("src/test/resources/testPlans/mapper.yaml")

  import org.corerda.rules.core.Mapper._
  describe("Job[Int] Test") {
    describe("given a job, foldTree ") {
      it("Should resolve the function composition defined in Tree[Task[Int]]") {
        import org.corerda.service.types.IntegerImpl
        import org.corerda.service.types.IntegerImpl._

        implicit val _ = IntegerImpl.taskDecoder
        val expected = Map(
          "node1" -> Node(Zero, ReaderCmp(11, "source_A")),
          "node3" -> Node(One("node1"), WriterCmp("sink_A")),
          "node4" -> Node(Zero, ReaderCmp(50, "source_B")),
          "node5" -> Node(One("node4"), FxCmp(List("if_div:3"), "left")),
          "node6" -> Node(One("node4"), FxCmp(List("if_div:7"), "left")),
          "node7" -> Node(Two("node5", "node6"), BinderCmp("enqueue")),
          "node8" -> Node(One("node7"), WriterCmp("sink_B")))

        assert(fromString[IntegerImpl.myType](myStrPlan) == expected)
      }
    }
  }
}
