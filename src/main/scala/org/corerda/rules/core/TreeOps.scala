package org.corerda.rules.core

import cats.Eval
import cats.syntax.all._
import org.corerda.entities._
import scala.collection.mutable

object TreeOps {
  class PredicateW[A](self: A => Boolean) {
    def and(other: A => Boolean): A => Boolean = a => self(a) && other(a)
    def or(other: A => Boolean): A => Boolean = a => self(a) || other(a)
    def unary_! : A => Boolean = a => !self(a)
  }

  /** Executes the graph of nodes and returns the roots of the expression trees.
   * Uses cats.Eval for stack-safety and memoization to handle DAG structures.
   * Returns an Either[Throwable, List[ExprTree[T]]] for pure error handling.
   */
  def runAST[T](graph: Map[String, Node[T]]): Either[Throwable, List[ExprTree[T]]] = {
    val memo = mutable.Map.empty[String, Either[Throwable, Eval[T]]]

    def evalNode(id: String): Either[Throwable, Eval[T]] = {
      memo.get(id) match {
        case Some(result) => result
        case None =>
          val result = graph.get(id) match {
            case None => Left(new IllegalArgumentException(s"Node $id not found in graph"))
            case Some(node) =>
              node match {
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
          }
          val memoizedResult = result.map(_.memoize)
          memo.put(id, memoizedResult)
          memoizedResult
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
