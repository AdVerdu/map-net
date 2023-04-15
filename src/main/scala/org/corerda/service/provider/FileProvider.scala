package org.corerda.service.provider

object FileProvider {
  // get plan from FS path
  def fromPath(path: String): String = {
    val source = scala.io.Source.fromFile(path)
    val yamlPlan = source.mkString
    source.close()

    yamlPlan
  }
}
