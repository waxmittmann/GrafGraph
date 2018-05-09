package io.grafgraph.render
import cats.data.NonEmptyList
import io.grafdefinition.WithBuilders
import io.grafgraph.example.{Attr, Attribute, Lake}
import java.io

object GraphCrudRenderer {

  /*
  import java.util.UUID
   */

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

  def renderAttribute(prefix: String)(attr: Attribute): String = {
    s"$prefix${attr.name}: ${attrType(attr)}${attrValue(attr)}"
  }

  private def uncapitalize(str: String) = str.head.toLower + str.tail

  def renderEdge(prefix: String)(selfName: String)(e: WithBuilders[Attribute]#Edge): String = indent(2)({
    //  def writeEdge(self: GraphDefinition[Attribute]#Vertex, e: GraphDefinition[Attribute]#Edge): String = {
    val r = e match {
      // Hmm ok, so here I need a concrete instance
      case Lake.OtherEdge(name: String, to: Lake.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Attribute]) => {
        // Ignore attribute for now
        // don't do optional; use toMany for that
        if (toMany) {
          s"${uncapitalize(name)}s: Seq[${to.name}]"
        } else {
          s"${uncapitalize(name)}: ${to.name}"
        }

      }

      case Lake.SelfEdge(name, attribute, optional, toMany) => {
        if (toMany) {
          s"${uncapitalize(name)}s: Seq[$selfName]"
        } else {
          s"${uncapitalize(name)}: $selfName"
        }

      }
    }

    s"$prefix$r"
  })

  def renderClazz(clazz: WithBuilders[Attribute]#Clazz): String = indent(2)({
    s"""
      |sealed trait ${clazz.name} {
      |${indent(2)(clazz.attributes.map(renderAttribute("val")).mkString(",\n"))}
      |${indent(2)(clazz.edges.map(renderEdge("val")(clazz.name)).mkString(",\n"))}
      |}
    """.stripMargin
  })

  def renderState(
    index: Int,
    vertex: WithBuilders[Attribute]#Vertex,
    state: WithBuilders[Attribute]#VertexState
  ): String = indent(2) {
//    val interfaces: Seq[String] = vertex.name :: vertex.clazz.map(_.name :: Nil).getOrElse(Nil)
    val interfaces: Seq[String] = vertex.clazz.map { c =>
      c.name :: vertex.name :: Nil
    }.getOrElse(
      vertex.name :: Nil
    )//vertex.name :: vertex.clazz.map(_.name :: Nil).getOrElse(Nil)

    val attrs = state.attributes.map(renderAttribute("")) ++: state.edges.map(renderEdge("")(vertex.name))

    val extendsPart = s"extends ${interfaces.head} ${interfaces.tail.map(i => s" with $i").mkString(",")}"
    s"""
       |case class ${state.name.getOrElse(s"State_$index")}(
       |
       |${indent(2)(attrs.mkString(",\n"))}
       |
       |) $extendsPart
     """.stripMargin
/*
${indent(2)(state.attributes.map(renderAttribute).mkString(",\n"))}
${indent(2)(state.edges.map(renderEdge(vertex.name)).mkString(",\n"))}

 */
  }


  def renderVersion(vertex: WithBuilders[Attribute]#Vertex, last: WithBuilders[Attribute]#VertexVersion): String =
    s"""
       |${indent(2)(last.allowedDefinitions.zipWithIndex.map { case (state, index) => renderState(index, vertex, state) }.toList.mkString("\n"))}
     """.stripMargin

  def renderVertex(vertex: WithBuilders[Attribute]#Vertex): String = indent(2)({
    s"""
       |sealed trait ${vertex.name}
       |
       |object ${vertex.name} {
       |
       |${indent(4)(renderVersion(vertex, vertex.versions.head))}
       |
       |}
    """.stripMargin
  })

  // Todo: Make all this configurable
  def render(graph: WithBuilders[Attribute]): String = {
    val packageName = "io.testgraph"
    val imports = Seq[String]("import java.util.UUID")

    //${renderMeta(graph.meta)}
    s"""
       |package $packageName
       |
       |${imports.map(i => s"import $i").mkString("\n")}
       |
       |object Graph {
       |  sealed trait CreateReadUpdate
       |  case class ReadUid(uid: UUID) extends CreateReadUpdate
       |  case class ReadQuery(query: String) extends CreateReadUpdate
       |  case class New[A](newA: A) extends CreateReadUpdate
       |
       |  // $${renderMeta(graph.meta)} // Not implemented
       |${indent(2)(graph.allClazzez.map(renderClazz).mkString("\n"))}
       |${indent(2)(graph.allVertices.map(renderVertex).mkString("\n"))}
       |}
     """.stripMargin
  }


  def indent(chars: Int)(str: String): String =
//    str
    str.linesWithSeparators.map(line => (" " take chars) + line).mkString//.mkString("\n")

}
