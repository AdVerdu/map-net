package org.corerda.service.provider


// Implemented by an IO ADT
trait Connector[A] {
  def read: A
  def write(data: A): Unit
}

// TODO - create ADT (?)