package org.corerda.rules.core

import org.corerda.entities._
import org.scalatest.funspec.AnyFunSpec
import cats.Eval

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

  import org.corerda.rules.core.TreeOps._
  describe("TreeOps Test") {
      describe("given a plan (graph as Map of nodes), runAST[T] ") {
        it("Should return the root results of the AST evaluation") {
          val result = runAST(myPlan)
          assert(result.isRight)
          val roots = result.toOption.get
          assert(roots.size == 2)

          val sink_A_res = roots.find(_.eval.value == (1 to 10).toList)
          assert(sink_A_res.isDefined)

          val sink_B_res = roots.find(_.eval.value == List(10, 20, 30, 40, 50, 60, 70, 24, 27, 30, 33, 36, 39, 42, 45, 48))
          assert(sink_B_res.isDefined)
        }
      }
  }
}
