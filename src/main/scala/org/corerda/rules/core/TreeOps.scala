package org.corerda.rules.core

import org.corerda.entities._
import org.corerda.rules.core.Job.Job

import scala.language.implicitConversions

object TreeOps {
  class PredicateW[A](self: A => Boolean) {
    def and(other: A => Boolean): A => Boolean = a => self(a) && other(a)
    def or(other: A => Boolean): A => Boolean = a => self(a) || other(a)
    def unary_! : A => Boolean = a => !self(a)
  }

  def toTree[T](graph: Map[String, Node[T]]): List[Job[T]] = {
    val bToC: Node[T] => Task[T] = _.config

    // get cardinal
    val pointer = (e: Node[T]) => e.predecessor match {
      case Zero => Nil
      case One(value) => List(value)
      case Two(left, right) => List(left, right)
    }

    val nonStarters: Set[String] = graph.values.flatMap(pointer).toSet

    implicit def Predicate_Is_PredicateW[A](p: A => Boolean): PredicateW[A] = new PredicateW(p)
    val starters = graph.view.filterKeys(!nonStarters)

    def buildTree(parent: Node[T]): Job[T] = {
      parent.predecessor match {
        case Zero => Tree.leaf(bToC(parent))
        case One(value) => Tree.stem(bToC(parent), buildTree(graph(value)))
        case Two(left, right) =>
          Tree.branch(bToC(parent), buildTree(graph(left)), buildTree(graph(right)))
      }
    }

    starters.values.map(buildTree).toList
  }
}
