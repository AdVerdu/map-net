package org.corerda.rules.core

import org.corerda.entities._
import org.scalatest.funspec.AnyFunSpec


class JobTest extends AnyFunSpec {
  import org.corerda.service.types.IntegerImpl._

  val myJob =
    Tree.stem(WriterCmp("sink_C"),
      Tree.branch(BinderCmp("merge"),
        Tree.stem(FxCmp(List("if_div:3"), "left"),
          Tree.leaf(ReaderCmp(50, "source_B"))),
        Tree.stem(FxCmp(List("if_div:7"), "left"),
          Tree.leaf(ReaderCmp(50, "source_B")))))

  // test type = List[Int]
  import org.corerda.rules.core.Job._
  describe("Job[T] Test") {
    describe("given a job, foldTree ") {
      it("Should resolve the function composition defined in Tree[Task[T]]") {
        val expected =
          List(10, 20, 30, 40, 50, 60, 70, 24, 27, 30, 33, 36, 39, 42, 45, 48)

        assert(foldTree(myJob) == expected)
      }
    }
  }
}
