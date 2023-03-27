package org.corerda.service.core

import org.corerda.entities._

object Job {
  type Job[A] = Tree[Task[A]]
  def foldTree[A](job: Job[A]): A = job match {
    case Leaf(task) => task.source
    case Stick(task, next) => task.f(foldTree(next))
    case Branch(task, left, right) => task.bind(foldTree(left), foldTree(right))
  }
}

