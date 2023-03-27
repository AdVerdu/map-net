package org.corerda.service.core

trait Task[A] {
  def source: A
  def f(predecessor: A): A
  def bind(leftPdc: A, rightPdc: A): A
}

