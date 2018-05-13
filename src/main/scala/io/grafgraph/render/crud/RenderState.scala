package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.{Attr, Attribute}
import io.grafgraph.render.crud.RenderAttribute.renderAttribute
import io.grafgraph.render.crud.RenderEdge.renderEdge
import io.grafgraph.render.crud.Util.indent

object RenderState {

  def renderState(
    index: Int, singleState: Boolean,
    vertex: WithBuilders#Vertex,
    state: WithBuilders#VertexState
  ): String = indent(2) {
    val interfaces: Seq[String] = vertex.clazz.map { c =>
      c.name :: vertex.name :: s"New" :: Nil
    }.getOrElse(
      vertex.name :: s"New" :: Nil
    )

    val attrs = state.allAttributes.map(renderAttribute("")) ++: state.edges.map(renderEdge("")(vertex.name))

    val extendsPart = interfaces match {
      case (ls: List[String]) => s"extends ${ls.mkString(" with ")}"
      case Nil => ""
    }

    val name = state.name

    s"""
       |case class $name(
       |
       |${indent(2)(attrs.mkString(",\n"))}
       |
       |) $extendsPart
       |
       |${renderStateCreate(vertex, state)}
       """.stripMargin
  }


  private def renderStateCreate(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    val renderParams = genRenderParams(vertex, state)

    val query =
      s"""
         ||CREATE (${state.name}: Class_${state.name} {${renderCreateAttributes(vertex, state)}})
       """.stripMargin

    // Todo: Return object
    //def create(ele: ${state.name}): ${state.name} = {
    s"""
       |def create(ele: ${state.name}): Unit = {
       |  ${neo4jTx(query, renderParams)}
       |}
       """.stripMargin
  }

  private def genRenderParams(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    val params = state.allAttributes.map { (attr: Attribute) =>
      s""""${attr.name}" -> ${genRenderParam(vertex, state, attr)}"""
    }.mkString(",")

    s"""
       |Map[String, Object](
       |  $params
       |).asJava
       """.stripMargin
  }

  def genRenderParam(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState, attr: Attribute): String = attr match {
    case Attr.UID(name) => s"ele.$name.toString"
    case Attr.String(name, value) => value.getOrElse(s"ele.$name")
    case Attr.Int(name, value) => value.map(_.toString).getOrElse(s"ele.$name")
    case Attr.Boolean(name, value) => value.map(b => s"$b.booleanValue().asInstanceOf[Object]").getOrElse(s"ele.$name")
  }

  private def renderCreateAttributes(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    state.allAttributes.map(renderCreateAttribute).mkString(", ")
  }

  private def renderCreateAttribute(attr: Attribute): String = s"${attr.name}: {${attr.name}}"

  private def neo4jTx(query: String, params: String): String =
    s"""
       |val session = graph.driver.session()
       |
         |session.writeTransaction { tx =>
       |  val params = $params
       |
         |  tx.run(\"\"\"$query\"\"\".stripMargin, params)
       |  tx.success()
       |}
       |session.close()
         """.stripMargin

}
