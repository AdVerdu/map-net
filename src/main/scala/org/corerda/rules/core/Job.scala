package org.corerda.rules.core

import org.corerda.entities._


// @deprecated
object Job {
  type Job[A] = Tree[Task[A]]
  def eval[A](job: Job[A]): A = job match {
    case Leaf(task: Reader[A]) => task.read
    case Stem(task: Writer[A], next) => task.write(eval(next))
    case Stem(task: Transformer[A], next) => task.f(eval(next))
    case Branch(task: Binder[A], left, right) =>
      task.bind(eval(left), eval(right))
  }
}