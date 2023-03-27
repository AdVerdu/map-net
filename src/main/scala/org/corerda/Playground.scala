package org.corerda

import org.corerda.entities._

import scala.language.implicitConversions

// TODO:
//  - organize code
//  - add more UT
//  - add CFG entities
//  - extract and validate graph
//  - Generate Jobs (Planner)
//  - read from config yaml
object Playground {

  class PredicateW[A](self: A => Boolean) {
    def and(other: A => Boolean): A => Boolean = a => self(a) && other(a)
    def or(other: A => Boolean): A => Boolean = a => self(a) || other(a)
    def unary_! : A => Boolean = a => !self(a)
  }

  def main(args: Array[String]): Unit = {
    // TODO:
    //  - Boot input params (what cfg to load? context execution?)
    //  - Encoder/Decoder from source configs (BDD, FS, whatever) (JSON, CSV, whatever)
    //    - Map[id, tid, Cfg]
    //  - buildTree & validations
    //    - List[Tree[Component]] (Do I need Component intermediate state? can I map directly into Job?)
    //  - mapTree
    //    - List[Job]
    //  - foldTree (execution)

    val graph: Map[String, TaskCfg] = Map(
      "n1" -> TaskCfg(Zero, ReaderCmp(10, "source_A")),
      "n2" -> TaskCfg(One("n1"), SplitterCmp(List("div_by_two"), "left")),
      "n3" -> TaskCfg(One("n2"), WriterCmp("sink_A")),
      "n4" -> TaskCfg(One("n2"), WriterCmp("sink_B")))

    // def mapTree[B, C](m: Map[String, B])(implicit transformer: B => C): List[Tree[C]] = {
    def toTree(m: Map[String, TaskCfg]): List[Tree[Component]] = {

      val bToC: TaskCfg => Component = _.config
      // get cardinal
      val pointer = (e: TaskCfg) => e.predecessor match {
        case Zero => Nil
        case One(value) => List(value)
        case Two(left, right) => List(left, right)
      }

      // m.view.mapValues
      // TODO - for comprehension?
      val nonStarters: Set[String] = graph.values.flatMap(pointer).toSet
      implicit def Predicate_Is_PredicateW[A](p: A => Boolean): PredicateW[A] = new PredicateW(p)
      val starters = graph.view.filterKeys(!nonStarters)


      def buildTree(prnt: TaskCfg): Tree[Component] = {
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
  }
}
