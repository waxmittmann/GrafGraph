package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.{Attr, Attribute}
import io.grafgraph.example.Lake
import io.grafgraph.render.crud.RenderState.renderNeo4jNodePart

object RenderCreateMethod2 {

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

  def renderCreateMethod(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    val params = renderParams(vertex, state)

    val edges = state.edges.flatMap { _ => Some() }

    val query =
      s"""
         |CREATE ${renderNeo4jNodePart(vertex, state)}
         |$edges
       """.stripMargin

    // Todo: Return object
    //def create(ele: ${state.name}): ${state.name} = {
    s"""
       |def create(ele: ${state.name}): Unit = {
       |  ${renderNeo4jTx(query, params)}
       |}
       """.stripMargin
  }

  private def renderParams(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState): String = {
    val params = state.allAttributes.map { (attr: Attribute) =>
      s""""${attr.name}" -> ${renderParam(vertex, state, attr)}"""
    }.mkString(",")

    s"""
       |Map[String, Object](
       |  $params
       |).asJava
       """.stripMargin
  }

  private def renderParam(vertex: WithBuilders#Vertex, state: WithBuilders#VertexState, attr: Attribute): String =
    attr match {
      case Attr.UID(name) => s"ele.$name.toString"
      case Attr.String(name, value) => value.getOrElse(s"ele.$name")
      case Attr.Int(name, value) => value.map(_.toString).getOrElse(s"ele.$name")
      case Attr.Boolean(name, value) => value.map(b => s"$b.booleanValue().asInstanceOf[Object]").getOrElse(s"ele.$name")
    }

  def renderEdgeHere(
    vertex: WithBuilders#Vertex,
    state: WithBuilders#VertexState
  )(edge: WithBuilders#Edge): String = ???

  private def renderNeo4jTx(query: String, params: String): String =
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
