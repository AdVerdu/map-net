package org.corerda.entities

import cats.Eval

case class ExprTree[+A](eval: Eval[A])

// Tag-less Final FoldedTree
object ExprTree {
  def leaf[A](ops: Reader[A]): ExprTree[A] = ExprTree(Eval.later(ops.read))
  def stem[A](ops: Transformer[A])(prev: ExprTree[A]): ExprTree[A] = ExprTree(prev.eval.map(ops.f))
  def branch[A](ops: Binder[A])(left: ExprTree[A], right: ExprTree[A]): ExprTree[A] =
    ExprTree(for {
      l <- left.eval
      r <- right.eval
    } yield ops.bind(l, r))
  def root[A](ops: Writer[A])(prev: ExprTree[A]): ExprTree[A] = ExprTree(prev.eval.map(ops.write))
}
