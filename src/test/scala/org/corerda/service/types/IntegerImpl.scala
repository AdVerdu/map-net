package org.corerda.service.types

import io.circe.generic.auto._
import io.circe.Decoder
import org.corerda.entities._

// TODO Service HOFs defined/enforced by framework (IO/f(_: *))
object IntegerImpl {
  // def notDefined: IllegalArgumentException = new IllegalArgumentException(" this isn't allowed, still I'll have to improve my code in order to make it impossible to happen")
  // Types TBD by user
  type myType = List[Int]

  // Service implementation are described by user
  case class ReaderCmp(size: Int, tag: String) extends Reader[myType] {
    def read: myType = (1 to size).toList
  }

  case class FxCmp(transformer: List[String], tag: String) extends Transformer[myType] {
    val lookup: List[myType => myType] =
      transformer.map {
        case "div_by_two" => (elem: myType) => elem.map(_ / 2)
        case s"if_div:$num" => (elem: myType) => elem.filter(_ % num.toInt == 0)
      }

    def f(data: myType): myType = lookup.foldLeft(data)((ca, f) => f(ca))
  }
  case class BinderCmp(tag: String) extends Binder[myType] {
    val operation: (myType, myType) => myType =
      tag match {
        case "enqueue" => (left, right) => left ++ right
        case "merge" => (left, right) =>
          val s = left.size.max(right.size)
          val collection = for {
            i <- 0 until s
          } yield left.lift(i).getOrElse(0) + right.lift(i).getOrElse(0)
          collection.toList
      }
    def bind(left: myType, right: myType): myType = operation(left, right)
  }
  case class WriterCmp(tag: String) extends Writer[myType] {
    def write(data: myType): myType = {
      println(s"the result in $tag is: $data")
      data
    }
  }


  // Service Decoder mapped by user (can be automated with reflection/PM of sorts?)
  // Decoder for Task
  implicit val taskDecoder: Decoder[Node[myType]] = taskCursor =>
    taskCursor.get[String]("type") match {
      case Right("input") =>
        for {
          reader <- taskCursor.get[ReaderCmp]("config")
        } yield Node(Zero, reader)
      case Right("operations") =>
        for {
          from <- taskCursor.get[String]("from")
          splitter <- taskCursor.get[FxCmp]("config")
        } yield Node(One(from), splitter)
      case Right("binder") =>
        for {
          left <- taskCursor.get[String]("left")
          right <- taskCursor.get[String]("right")
          binder <- taskCursor.get[BinderCmp]("config")
        } yield Node(Two(left, right), binder)
      case Right("output") =>
        for {
          from <- taskCursor.get[String]("from")
          writer <- taskCursor.get[WriterCmp]("config")
        } yield Node(One(from), writer)
    }
}
