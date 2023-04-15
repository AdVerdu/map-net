package org.corerda.entities

sealed trait Task[T]
trait Reader[T] extends Task[T] {
  def read: T
}
trait Transformer[T] extends Task[T] {
  def f(data: T): T
}
trait Binder[T] extends Task[T] {
  def bind(left: T, right: T): T
}
trait Writer[T] extends Task[T] {
  def write(data: T): T
}
