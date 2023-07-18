package org.corerda.entities


case class ExprTree[+A](value: A)

// TODO - review semantic redundancy with Task and Cardinal
// Tag-less Final FoldedTree
object ExprTree {
  def leaf[A](ops: Reader[A]): ExprTree[A] = ExprTree(ops.read)
  def stem[A](ops: Transformer[A])(prev: ExprTree[A]): ExprTree[A] = ExprTree(ops.f(prev.value))
  def branch[A](ops: Binder[A])(left: ExprTree[A], right: ExprTree[A]): ExprTree[A] = ExprTree(ops.bind(left.value, right.value))
  def root[A](ops: Writer[A])(prev: ExprTree[A]): ExprTree[A] = ExprTree(ops.write(prev.value))
}