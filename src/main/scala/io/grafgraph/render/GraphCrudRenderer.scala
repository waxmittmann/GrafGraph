package io.grafgraph.render
import cats.data.NonEmptyList
import io.grafdefinition.WithBuilders
import io.grafgraph.example.{Attr, Attribute, Lake}
import java.io

object GraphCrudRenderer {

  def attrType(attr: Attribute): String = attr match {
    case Attr.Int(_, _) => "Int"
    case Attr.String(_, _) => "String"
    case Attr.UID(_, _) => "String"
    case Attr.Boolean(_, _) => "Boolean"
  }

  def attrValue(attr: Attribute): String = attr match {
    case Attr.Int(_, Some(value)) => s"= $value"
    case Attr.String(_, Some(value)) => s"""= "$value""""
    case Attr.UID(_, Some(value)) => s"""= "$value""""
    case Attr.Boolean(_, Some(value)) => if (value) "= true" else "= false"
    case _ => ""
  }

  def renderAttribute(attr: Attribute): String = {
    s"val ${attr.name}: ${attrType(attr)}${attrValue(attr)},"
  }

  private def uncapitalize(str: String) = str.head.toLower + str.tail

  def renderEdge(selfName: String)(e: WithBuilders[Attribute]#Edge): String = indent(2)({
    //  def writeEdge(self: GraphDefinition[Attribute]#Vertex, e: GraphDefinition[Attribute]#Edge): String = {
    e match {
      // Hmm ok, so here I need a concrete instance
      case Lake.OtherEdge(name: String, to: Lake.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Attribute]) => {
        // Ignore attribute for now
        // don't do optional; use toMany for that
        if (toMany) {
          s"val ${uncapitalize(name)}s: Seq[${to.name}]"
        } else {
          s"val ${uncapitalize(name)}: ${to.name}]"
        }

      }

      case Lake.SelfEdge(name, attribute, optional, toMany) => {
        if (toMany) {
          s"val ${uncapitalize(name)}s: Seq[${selfName}]"
        } else {
          s"val ${uncapitalize(name)}: ${selfName}"
        }

      }
    }
  })

  def renderClazz(clazz: WithBuilders[Attribute]#Clazz): String = indent(2)({
    s"""
      |sealed trait ${clazz.name} {
      |${indent(2)(clazz.attributes.map(renderAttribute).mkString("\n"))}
      |${indent(2)(clazz.edges.map(renderEdge(clazz.name)).mkString("\n"))}
      |}
    """.stripMargin
  })

  def renderState(
    vertex: WithBuilders[Attribute]#Vertex
  )(
    state: WithBuilders[Attribute]#VertexState
  ): String = indent(2) {
//    val interfaces: Seq[String] = vertex.name :: vertex.clazz.map(_.name :: Nil).getOrElse(Nil)
    val interfaces: Seq[String] = vertex.clazz.map { c =>
      c.name :: vertex.name :: Nil
    }.getOrElse(
      vertex.name :: Nil
    )//vertex.name :: vertex.clazz.map(_.name :: Nil).getOrElse(Nil)

    s"""
       |case class ${state.name.getOrElse("BAD!")} implements ${interfaces.head} ${interfaces.tail.map(i => s" with $i").mkString(",")} {
       |${indent(2)(state.attributes.map(renderAttribute).mkString("\n"))}
       |${indent(2)(state.edges.map(renderEdge(vertex.name)).mkString("\n"))}
       |}
     """.stripMargin

  }


  def renderVersion(vertex: WithBuilders[Attribute]#Vertex, last: WithBuilders[Attribute]#VertexVersion): String =
    s"""
       |${indent(2)(last.allowedDefinitions.map(renderState(vertex)).toList.mkString("\n"))}
     """.stripMargin

  def renderVertex(vertex: WithBuilders[Attribute]#Vertex): String = indent(2)({
    s"""
       |sealed trait ${vertex.name} {
       |${indent(2)(renderVersion(vertex, vertex.versions.head))}
       |}
    """.stripMargin
  })

  def render(graph: WithBuilders[Attribute]): String = {
    //${renderMeta(graph.meta)}
    s"""
       |object Graph {
       |  $${renderMeta(graph.meta)} \\ Not implemented
       |${indent(2)(graph.allClazzez.map(renderClazz).mkString("\n"))}
       |${indent(2)(graph.allVertices.map(renderVertex).mkString("\n"))}
       |}
     """.stripMargin
  }


  def indent(chars: Int)(str: String): String =
//    str
    str.linesWithSeparators.map(line => (" " take chars) + line).mkString//.mkString("\n")

}
