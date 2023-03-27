package org.corerda.entities

// TODO - Generalize components? move to service layer as IntComponent?
sealed trait Component
case class ReaderCmp(size: Int, tag: String) extends Component
case class SplitterCmp(transformer: List[String], tag: String) extends Component
case class WriterCmp(tag: String) extends Component
