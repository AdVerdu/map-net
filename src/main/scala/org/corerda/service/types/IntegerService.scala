package org.corerda.service.types

import org.corerda.service.core.Task
import org.corerda.service.provider.Connector

object IntegerService {
  def notDefined: IllegalArgumentException = new IllegalArgumentException(" this isn't allowed, still I'll have to improve my code in order to make it impossible to happen")

  type myCol = List[Int]

  trait IntegerOps extends Task[myCol]

  case class Reader(conn: Connector[myCol]) extends IntegerOps {
    override def source: myCol = conn.read

    override def f(predecessor: myCol): myCol = throw notDefined

    override def bind(leftPdc: myCol, rightPdc: myCol): myCol = throw notDefined
  }

  case class Writer(conn: Connector[myCol]) extends IntegerOps {
    override def source: myCol = throw notDefined

    override def f(predecessor: myCol): myCol = {
      conn.write(predecessor)
      predecessor
    }

    override def bind(leftPdc: myCol, rightPdc: myCol): myCol = throw notDefined
  }

  case class Splitter(func: myCol => (myCol, myCol), predicate: Boolean) extends IntegerOps {
    override def source: myCol = throw notDefined

    override def f(predecessor: myCol): myCol = if (predicate) func(predecessor)._2 else func(predecessor)._1

    override def bind(leftPdc: myCol, rightPdc: myCol): myCol = throw notDefined
  }

  case class Caster(func: myCol => myCol) extends IntegerOps {
    override def source: myCol = throw notDefined

    override def f(predecessor: myCol): myCol = func(predecessor)

    override def bind(leftPdc: myCol, rightPdc: myCol): myCol = throw notDefined
  }

  // TODO - simplify
  trait Ternary

  trait Left extends Ternary

  trait Inner extends Ternary

  trait Right extends Ternary

  //  case class Binder(func: (myCol, myCol) => myCol, ternary: Ternary) extends IntegerOps {
  //    override def source: myCol = throw notDefined
  //    override def f(predecessor: myCol): myCol = throw notDefined
  //    override def bind(leftPdc: myCol, rightPdc: myCol): myCol = func(leftPdc, rightPdc)
  //  }

  // TODO - Implement PBS for reading
  case class IntConnector(size: Int, tag: String) extends Connector[myCol] {
    override def read: myCol = (0 to size).toList

    override def write(data: myCol): Unit = println(s"the result in $tag is: $data")
  }
}
