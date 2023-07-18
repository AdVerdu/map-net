package org.corerda.rules.core

import org.corerda.entities._

import scala.language.implicitConversions

object TreeOps {
  class PredicateW[A](self: A => Boolean) {
    def and(other: A => Boolean): A => Boolean = a => self(a) && other(a)
    def or(other: A => Boolean): A => Boolean = a => self(a) || other(a)
    def unary_! : A => Boolean = a => !self(a)
  }

  // FIXME - rename to something like Run
  // TODO - return a list of validations/errors per ExprTree executed
  def runAST[T](graph: Map[String, Node[T]]): List[ExprTree[T]] = {
    // get cardinal
    val pointed = (e: Node[T]) => e.predecessor match {
      case Zero => Nil
      case One(value) => List(value)
      case Two(left, right) => List(left, right)
    }

    // @ comment: get a list of all the distinct nodes that are being pointed at
    val nonStarters: Set[String] = graph.values.flatMap(pointed).toSet

    implicit def predicateWrapper[A](p: A => Boolean): PredicateW[A] = new PredicateW(p)
    // @ comment: get the root elements of the trees
    val starters = graph.view.filterKeys(!nonStarters)

    // TODO
    //  - tailrec version (?)
    //  - expression problem
    //      - TF version (tree of expressions not objects)
    //      - Can we remove altogether the Tree skipping straight to the final composed function (?)
    def mapEval(parent: Node[T]): ExprTree[T] = {
      parent match {
        case Node(Zero, ops:Reader[T]) =>
          ExprTree.leaf(ops)
        case Node(One(child), ops: Transformer[T]) =>
          ExprTree.stem(ops)(mapEval(graph(child)))
        case Node(Two(left, right), ops: Binder[T]) =>
          ExprTree.branch(ops)(mapEval(graph(left)), mapEval(graph(right)))
        // FIXME - warn /!\ this triggers actions (it's that okay?)
        case Node(One(child), ops: Writer[T])=>
          ExprTree.root(ops)(mapEval(graph(child)))
      }
    }

    // @ comment: run the function composition of the Expression Trees from the list of roots
    starters.values.map(mapEval).toList
  }
}
