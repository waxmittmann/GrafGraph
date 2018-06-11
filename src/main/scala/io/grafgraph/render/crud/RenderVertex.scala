package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.render.crud
import io.grafgraph.render.crud.Util.{indent, uncapitalize}

/*
    def renderVersion(vertex: WithBuilders#Vertex, last: WithBuilders#VertexState): String =
      s"""
         |
         |  case class ByUid(uid: UUID) extends VertexByUid with ${vertex.name}
         |  case class ByQuery(query: String) extends VertexByQuery with ${vertex.name}
         |  sealed trait New extends VertexNew[${vertex.name}]
         |
         |${indent(2)(last.states.zipWithIndex.map { case (state, index) =>
        RenderState.renderState(index, last.states.length == 1, vertex, state)
      }.toList.mkString("\n"))}
       """.stripMargin

 */
object RenderVertex {

    def renderVertex(vertex: WithBuilders#Vertex): String = {
      indent(2)({
        s"""
           |object ${vertex.name} {
           |  sealed trait ${vertex.name}
           |  case class ByUid(uid: UUID) extends VertexByUid with ${vertex.name}
           |  case class ByQuery(query: String) extends VertexByQuery with ${vertex.name}
           |  sealed trait ByShape extends VertexNew[${vertex.name}]
           |
           |${
            indent(2)(vertex.states.zipWithIndex.map { case (state, index) =>
              crud.RenderState.renderState(index, vertex.states.length == 1, vertex, state)
              }.toList.mkString("\n"))
            }
           |
           |
           |
           |  //def create(new${uncapitalize(vertex.name)}: New): New = ???
           |
           |}
      """.stripMargin
      })
    }

  //${indent(2)(RenderState.renderState(vertex, vertex.states))}
}
