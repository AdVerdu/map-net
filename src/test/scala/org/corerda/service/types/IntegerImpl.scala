package org.corerda.service.types

import io.circe.generic.auto._
import io.circe.Decoder
import org.corerda.entities._

object IntegerImpl {
  type myType = List[Int]

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

  implicit val taskDecoder: Decoder[Node[myType]] = taskCursor =>
    taskCursor.get[String]("type").flatMap {
      case "input"      => taskCursor.get[ReaderCmp]("config").map(Node(Zero, _))
      case "operations" => for {
                             from <- taskCursor.get[String]("from")
                             fx   <- taskCursor.get[FxCmp]("config")
                           } yield Node(One(from), fx)
      case "binder"     => for {
                             l      <- taskCursor.get[String]("left")
                             r      <- taskCursor.get[String]("right")
                             binder <- taskCursor.get[BinderCmp]("config")
                           } yield Node(Two(l, r), binder)
      case "output"     => for {
                             from <- taskCursor.get[String]("from")
                             writer <- taskCursor.get[WriterCmp]("config")
                           } yield Node(One(from), writer)
      case other        => Left(io.circe.DecodingFailure(s"Unknown task type: $other", taskCursor.history))
    }
}
