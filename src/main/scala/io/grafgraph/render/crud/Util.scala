package io.grafgraph.render.crud

object Util {
  def uncapitalize(str: String): String = str.head.toLower + str.tail

  def indent(chars: Int)(str: String): String =
    str.linesWithSeparators.map(line => (" " take chars) + line).mkString//.mkString("\n")
}
