package org.corerda.entities


// TODO - make it generic, apply given an implicit encoder from configuration => T
case class TaskCfg(predecessor: Cardinal[String], config: Component)
/*
object TaskCfg {
  def apply(component: String, predecessor: Option[String], config: Map[String, String]): Option[TaskCfg] = {
    val cmp: Component = component match {
      case "reader" => ReaderCmp(config.getOrElse("size", "0").toInt, config.getOrElse("tag", "whatever"))
      case "splitter" => SplitterCmp(config.getOrElse("list", "").split(",").toList, config.getOrElse("tag", "left"))
      case "writer" => WriterCmp(config.getOrElse("tag", "whatever"))
      case _ => throw new RuntimeException
    }
    Option(TaskCfg(predecessor, cmp))
  }
}
 */
