package org.corerda.entities

trait Tree[+A]
object Tree {
  def leaf[A](value: A): Tree[A] = Leaf(value)
  def stick[A](value: A, next: Tree[A]): Tree[A] = Stick(value, next)
  def branch[A](value: A, left: Tree[A], right: Tree[A]): Tree[A] = Branch(value, left, right)
}

case class Leaf[+A](value: A) extends Tree[A]
case class Stick[+A](value: A, next: Tree[A]) extends Tree[A]
case class Branch[+A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]

