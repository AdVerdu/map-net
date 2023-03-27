package org.corerda.entities

// TODO - too many things to fix here but let's go with the flow and improve later
sealed trait Cardinal[+A]
case object Zero extends Cardinal[Nothing] with Product with Serializable
final case class One[+A](value: A) extends Cardinal[A] with Product with Serializable
final case class Two[+A](left: A, right: A) extends Cardinal[A] with Product with Serializable
