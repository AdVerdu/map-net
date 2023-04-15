package org.corerda.entities

case class Plan[T](id: Int,
                   name: String,
                   env: String,
                   version: String,
                   tasks: Map[String, Node[T]])
