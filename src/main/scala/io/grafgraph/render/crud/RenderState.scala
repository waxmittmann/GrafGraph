package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.{Attr, Attribute}
import io.grafgraph.example.Simple
import io.grafgraph.render.crud.RenderAttribute.renderAttribute
import io.grafgraph.render.crud.RenderEdge.renderEdgeForDao
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

    val attrs = state.allAttributes.map(renderAttribute("")) ++: state.edges.map(renderEdgeForDao("")(vertex.name))

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
       |${RenderCreateMethod.renderCreateMethod(vertex, state)}
       """.stripMargin
  }

  def renderNeo4jNodePart(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String =
    s"(${state.name}: Class_${state.name} {${renderCypherAttributes(vertex, state)}})"

//  private def renderGetNeo4jNodePartMethod(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String =
//    s"""
//       |def getNeo4jNodePart()
//     """.stripMargin

  /*
  private def renderCreateMethod(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    val params = renderParams(vertex, state)

    val edges = renderEdges(vertex, state)

    val query =
      s"""
         ||CREATE ${renderNeo4jNodePart(vertex, state)}
         ||$edges
       """.stripMargin

    // Todo: Return object
    //def create(ele: ${state.name}): ${state.name} = {
    s"""
       |def create(ele: ${state.name}): Unit = {
       |  ${renderNeo4jTx(query, params)}
       |}
       """.stripMargin
  }

  private def renderParam(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState, attr: Attribute): String =
    attr match {
      case Attr.UID(name) => s"ele.$name.toString"
      case Attr.String(name, value) => value.getOrElse(s"ele.$name")
      case Attr.Int(name, value) => value.map(_.toString).getOrElse(s"ele.$name")
      case Attr.Boolean(name, value) => value.map(b => s"$b.booleanValue().asInstanceOf[Object]").getOrElse(s"ele.$name")
    }

  def renderEdges(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String =
  //    state.edges.map(RenderEdge.renderEdgeForCypher(vertex, state)).mkString("\n")
    state.edges.map(renderEdgeHere(vertex, state)).mkString("\n")

  /*
              """
     MATCH (a: A {uid: {a.uid}}
     CREATE (complete: Class_Complete {uid: {uid}, status: {status}, jobUid: {jobUid}})
     (complete)->(a)
     (complete)->(b: B {uid: {b.uid}, some: {b.some}}
       (b)->(c: C {uid: {b.c.uid}, other: {b.c.other} )
       UNROLL b.ds AS b_d
       (b)->(d: D {uid: {b_d.uid}, other: {b_d.other} )
            """.stripMargin,
   */
  def renderEdgeHere(
    vertex: WithBuilders#Vertex,
    state: WithBuilders#VertexState
  )(edge: WithBuilders#Edge): String = {
    edge match {
      case Lake.OtherEdge(name, to, optional, toMany, attribute) => "???"

      case Lake.SelfEdge(name, attribute, optional, toMany) => "???"
    }
  }

*/

  private def renderCypherAttributes(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    state.allAttributes.map(renderCypherAttribute).mkString(", ")
  }

  private def renderCypherAttribute(attr: Attribute): String = s"${attr.name}: {${attr.name}}"

}
