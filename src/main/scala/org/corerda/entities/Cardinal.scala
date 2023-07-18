package org.corerda.entities

// the Cardinal number for Domain superset of the functions
// it's meant to hold the Task pointers
sealed trait Cardinal[+A]
case object Zero extends Cardinal[Nothing] with Product with Serializable
final case class One[+A](value: A) extends Cardinal[A] with Product with Serializable
final case class Two[+A](left: A, right: A) extends Cardinal[A] with Product with Serializable
