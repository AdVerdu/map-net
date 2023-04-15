package org.corerda.entities

case class Node[T](predecessor: Cardinal[String], config: Task[T])
