package org.corerda.rules.core

import cats.Eval
import cats.syntax.all._
import org.corerda.entities._

import scala.collection.mutable

object TreeOps {

  /** Executes the graph of nodes and returns the roots of the expression trees.
    * Uses cats.Eval for stack-safety and memoization to handle DAG structures.
    * Returns an Either[Throwable, List[ExprTree[T]]] for pure error handling.
    */
  def runAST[T](graph: Map[String, Node[T]]): Either[Throwable, List[ExprTree[T]]] = {
    val memo = mutable.Map.empty[String, Eval[T]]

    def evalNode(id: String): Either[Throwable, Eval[T]] = {
      memo.get(id) match {
        case Some(eval) => Right(eval)
        case None =>
          val node = graph.get(id).toRight(new IllegalArgumentException(s"Node $id not found in graph"))
          node.flatMap { n =>
            val evalOrError: Either[Throwable, Eval[T]] = n match {
              case Node(Zero, ops: Reader[T]) =>
                Right(Eval.later(ops.read))
              case Node(One(childId), ops: Transformer[T]) =>
                evalNode(childId).map(_.map(ops.f))
              case Node(Two(leftId, rightId), ops: Binder[T]) =>
                for {
                  lEval <- evalNode(leftId)
                  rEval <- evalNode(rightId)
                } yield for {
                  l <- lEval
                  r <- rEval
                } yield ops.bind(l, r)
              case Node(One(childId), ops: Writer[T]) =>
                evalNode(childId).map(_.map(ops.write))
              case _ =>
                Left(new IllegalArgumentException(s"Unsupported node configuration for node $id"))
            }
            evalOrError.map { eval =>
              val memoized = eval.memoize
              memo.put(id, memoized)
              memoized
            }
          }
      }
    }

    val pointed = (e: Node[T]) => e.predecessor match {
      case Zero => Nil
      case One(value) => List(value)
      case Two(left, right) => List(left, right)
    }

    val nonStarters: Set[String] = graph.values.flatMap(pointed).toSet
    val starters = graph.keys.filterNot(nonStarters).toList

    starters.traverse(id => evalNode(id).map(ExprTree(_)))
  }
}
