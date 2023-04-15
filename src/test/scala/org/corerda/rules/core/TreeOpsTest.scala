package org.corerda.rules.core

import org.corerda.entities._
import org.scalatest.funspec.AnyFunSpec


class TreeOpsTest extends AnyFunSpec {
  import org.corerda.service.types.IntegerImpl._

  val myPlan = Map(
    "node1" -> Node(Zero, ReaderCmp(10, "source_A")),
    "node2" -> Node(One("node1"), WriterCmp("sink_A")),
    "node4" -> Node(Zero, ReaderCmp(50, "source_B")),
    "node5" -> Node(One("node4"), FxCmp(List("if_div:3"), "left")),
    "node6" -> Node(One("node4"), FxCmp(List("if_div:7"), "left")),
    "node7" -> Node(Two("node5", "node6"), BinderCmp("merge")),
    "node8" -> Node(One("node7"), WriterCmp("sink_B")))

  // test type = List[Int]
  import org.corerda.rules.core.TreeOps._
  describe("TreeOps Test") {
    describe("given a plan (graph as Map of nodes), toTree[T] ") {
      it("Should split and parse into a List[Job[T]]") {
        val expected = List(
          Tree.stem(WriterCmp("sink_B"),
            Tree.branch(BinderCmp("merge"),
              Tree.stem(FxCmp(List("if_div:3"), "left"),
                Tree.leaf(ReaderCmp(50, "source_B"))),
              Tree.stem(FxCmp(List("if_div:7"), "left"),
                Tree.leaf(ReaderCmp(50, "source_B"))))),
          Tree.stem(WriterCmp("sink_A"),
            Tree.leaf(ReaderCmp(10, "source_A"))))
        assert(toTree(myPlan) == expected)
      }
    }
  }
}
