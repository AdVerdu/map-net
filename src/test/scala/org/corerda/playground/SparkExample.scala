package org.corerda.playground

import io.circe.generic.auto._
import io.circe.yaml._
import io.circe.{Decoder, Json, ParsingFailure}
import org.corerda.entities._
import scala.language.implicitConversions
import scala.util.{Failure, Success}

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkExample extends App {
  val spark = SparkSession.builder
    .appName("SparkExample")
    .config("spark.master", "local")
    .getOrCreate()

  type myType = DataFrame
  type Job = Tree[Cmpnt]

  case class Graph(id: Int, name: String, env: String, version: String, task: Map[String, Tasky])
  case class Tasky(predecessor: Cardinal[String], config: Cmpnt)

  sealed trait Cmpnt
  case class ReaderCmp(basePath: String, filters: Map[String, String], tag: String) extends Cmpnt {
    val filtersFormat = filters.map(f => s"${f._1}=${f._2}").mkString("/")
    def read: myType = spark.read
      .format("json")
      .option("basePath", basePath)
      .load(s"$basePath/$filtersFormat")
  }
  case class TransformerCmp(transformer: List[String], tag: String) extends Cmpnt {
    val lookup: List[myType => myType] =
      transformer.map {
        case s"$source:flat:$target" => (data: DataFrame) => data.withColumn(target, col(source))
        case s"$source:lit_$str:$target" => (data: DataFrame) => data.withColumn(target, concat(col(source), lit(s"_$str")))
        case s"$source:${str}_lit:$target" => (data: DataFrame) => data.withColumn(target, concat(lit(s"${str}_"), col(source)))
        case s"$left:rconcat:$right" => (data: DataFrame) => data.withColumn(left, concat(col(left), lit("-"), col(right)))
        case s"$source:explode_outer:$target" => (data: DataFrame) => data.withColumn(target, explode_outer(col(source)))
        case s"select:$keys" => (data: DataFrame) =>
          val cols = keys.split(",")
          data.select(cols.head, cols.tail: _*)
      }

    def f(data: myType): myType = lookup.foldLeft(data)((ca, f) => f(ca))
  }
  case class BinderCmp(mode: String, keys: Seq[String], tag: String) extends Cmpnt {
    val operation: (myType, myType) => myType =(left, right) => left.join(right, keys, mode)
    def bind(left: myType, right: myType): myType = operation(left, right)
  }
  case class WriterCmp(tag: String) extends Cmpnt {
    def write(data: myType): Unit = data.show
  }

  implicit val taskDecoder: Decoder[Tasky] = taskCursor =>
    taskCursor.get[String]("type") match {
      case Right("input") =>
        for {
          reader <- taskCursor.get[ReaderCmp]("config")
        } yield Tasky(Zero, reader)
      case Right("transformer") =>
        for {
          from <- taskCursor.get[String]("from")
          transf <- taskCursor.get[TransformerCmp]("config")
        } yield Tasky(One(from), transf)
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

  val source = scala.io.Source.fromFile("src/test/resources/playground/plans/sparkGraph.yaml")
  val yaml: Either[ParsingFailure, Json] = parser.parse(source.mkString)
  val plan: Either[io.circe.Error, Graph] = yaml.flatMap(_.as[Graph])
  val tasks: Graph = plan.toTry match {
    case Success(value) => value
    case Failure(e) => throw new Exception(e)
  }
  val graph = tasks.task
  source.close()

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
        case One(value) => Tree.stem(bToC(prnt), buildTree(graph(value)))
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
    case Stem(task: TransformerCmp, next) => task.f(foldTree(next))
    case Stem(writer: WriterCmp, next) =>
      val data = foldTree(next)
      writer.write(data)
      data
    case Branch(task: BinderCmp, left, right) => task.bind(foldTree(left), foldTree(right))
  }

  treeGraph.map(foldTree)
}