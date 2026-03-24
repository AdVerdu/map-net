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
      describe("given a plan (graph as Map of nodes), runAST[T] ") {
        it("Should return the root results of the AST evaluation") {
          val resultsOrError = runAST(myPlan)
          assert(resultsOrError.isRight)
          val results = resultsOrError.getOrElse(Nil)

          val expectedValues = Set(
            List(10, 20, 30, 40, 50, 60, 70, 24, 27, 30, 33, 36, 39, 42, 45, 48),
            List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
          )

          val actualValues = results.map(_.eval.value).toSet

          assert(actualValues == expectedValues)
        }
      }
    }
}
