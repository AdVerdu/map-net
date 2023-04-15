package org.corerda.rules.core

import org.corerda.entities._

object Job {
  type Job[A] = Tree[Task[A]]
  def foldTree[A](job: Job[A]): A = job match {
    case Leaf(task: Reader[A]) => task.read
    case Stem(task: Writer[A], next) => task.write(foldTree(next))
    case Stem(task: Transformer[A], next) => task.f(foldTree(next))
    case Branch(task: Binder[A], left, right) =>
      task.bind(foldTree(left), foldTree(right))
  }
}

