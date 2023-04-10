package org.corerda.playground

import io.circe.generic.auto._
import io.circe.yaml._
import io.circe.{Decoder, Json, ParsingFailure}
import org.corerda.entities._

import scala.language.implicitConversions
import scala.util.{Failure, Success}

// let user be whomever uses the lib
// let framework be this lib
object IntExample extends App {
  // Types TBD by user
  type myType = List[Int]
  type Job = Tree[Cmpnt]

  // Entities to be generic with params enforced by framework
  case class Graph(id: Int, name: String, env: String, version: String, task: Map[String, Tasky])
  case class Tasky(predecessor: Cardinal[String], config: Cmpnt)

  // Service HOFs defined/enforced by framework
  // Service implementation are described by user
  sealed trait Cmpnt
  case class ReaderCmp(size: Int, tag: String) extends Cmpnt {
    def read: myType = (1 to size).toList
  }
  case class SplitterCmp(transformer: List[String], tag: String) extends Cmpnt {
    val lookup: List[myType => myType] =
      transformer.map {
      case "div_by_two" => (elem: myType) => elem.map(_ / 2)
      case s"if_div:$num" => (elem: myType) => elem.filter(_ % num.toInt == 0)
    }

    def f(data: myType): myType = lookup.foldLeft(data)((ca, f) => f(ca))
  }
  case class BinderCmp(tag: String) extends Cmpnt {
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
  case class WriterCmp(tag: String) extends Cmpnt {
    def write(data: myType): Unit = println(s"the result in $tag is: $data")
  }

  // Service Decoder mapped by user (can be automated with reflection/PM of sorts?)
  // Decoder for Task
  implicit val taskDecoder: Decoder[Tasky] = taskCursor =>
    taskCursor.get[String]("type") match {
      case Right("input") =>
        for {
          reader <- taskCursor.get[ReaderCmp]("config")
        } yield Tasky(Zero, reader)
      case Right("splitter") =>
        for {
          from <- taskCursor.get[String]("from")
          splitter <- taskCursor.get[SplitterCmp]("config")
        } yield Tasky(One(from), splitter)
      case Right("binder") =>
        for {
          left <- taskCursor.get[String]("left")
          right <- taskCursor.get[String]("right")
          binder <- taskCursor.get[BinderCmp]("config")
        } yield Tasky(Two(left, right), binder)
      case Right("output") =>
        for {
          from <- taskCursor.get[String]("from")
          writer <- taskCursor.get[WriterCmp]("config")
        } yield Tasky(One(from), writer)
    }

  // Provider (YAML) format enforced by framework
  // Provider source/connector is implemented by user
  val source = scala.io.Source.fromFile("src/test/resources/playground/plans/intGraph.yaml")
  val yaml: Either[ParsingFailure, Json] = parser.parse(source.mkString)
  val plan: Either[io.circe.Error, Graph] = yaml.flatMap(_.as[Graph])
  val tasks: Graph = plan.toTry match {
    case Success(value) => value
    case Failure(e) => throw new Exception(e)
  }
  val graph = tasks.task
  source.close()

  // Rules are defined by framework
  class PredicateW[A](self: A => Boolean) {
    def and(other: A => Boolean): A => Boolean = a => self(a) && other(a)
    def or(other: A => Boolean): A => Boolean = a => self(a) || other(a)
    def unary_! : A => Boolean = a => !self(a)
  }

  def toTree(m: Map[String, Tasky]): List[Tree[Cmpnt]] = {
    val bToC: Tasky => Cmpnt = _.config
    // get cardinal
    val pointer = (e: Tasky) => e.predecessor match {
      case Zero => Nil
      case One(value) => List(value)
      case Two(left, right) => List(left, right)
    }

    val nonStarters: Set[String] = graph.values.flatMap(pointer).toSet

    implicit def Predicate_Is_PredicateW[A](p: A => Boolean): PredicateW[A] = new PredicateW(p)
    val starters = graph.view.filterKeys(!nonStarters)

    def buildTree(prnt: Tasky): Tree[Cmpnt] = {
      prnt.predecessor match {
        case Zero => Tree.leaf(bToC(prnt))
        case One(value) => Tree.stick(bToC(prnt), buildTree(graph(value)))
        case Two(left, right) =>
          Tree.branch(bToC(prnt), buildTree(graph(left)), buildTree(graph(right)))
      }
    }

    starters.values.map(buildTree).toList
  }

  val treeGraph = toTree(graph)
  println(treeGraph)

  // TODO - MapTree (from components to Tasks)
  def foldTree(job: Job): myType = job match {
    case Leaf(reader: ReaderCmp) => reader.read
    case Stick(task: SplitterCmp, next) => task.f(foldTree(next))
    case Stick(writer: WriterCmp, next) =>
      val data = foldTree(next)
      writer.write(data)
      data
    case Branch(task: BinderCmp, left, right) => task.bind(foldTree(left), foldTree(right))
  }

  treeGraph.map(foldTree)
}