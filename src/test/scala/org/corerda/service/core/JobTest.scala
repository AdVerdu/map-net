package org.corerda.service.core

import org.corerda.entities.Tree
import org.corerda.service.types.IntegerService._
import org.corerda.service.core.Job._
import org.scalatest.funspec.AnyFunSpec


class JobTest extends AnyFunSpec {

  val connector: IntConnector = IntConnector(10, "source_A")
  val connector2: IntConnector = IntConnector(0, "sink_A")
  val connector3: IntConnector = IntConnector(0, "sink_B")

  val job: Job[myCol] =
    Tree.stick(
      Splitter(c => c.partition(_ % 2 == 0), true),
      Tree.leaf(Reader(connector)))

  describe("Job Integer Test") {
    describe("given a job, foldTree ") {
      it("Should resolve the valued for the function composition and output the result") {
        assert(foldTree(job) == List(1, 3, 5, 7, 9))
        assert(foldTree(Tree.stick(
          Caster(_.map(_ + 1)), job)) == List(2, 4, 6, 8, 10))
      }
      it("Should _") {
        val planner: List[Job[myCol]] = List(
          Tree.stick(
            Writer(connector2), job),
          Tree.stick(
            Writer(connector3), job))

        assert(planner.foreach(foldTree) == ())
      }
    }
  }
}
