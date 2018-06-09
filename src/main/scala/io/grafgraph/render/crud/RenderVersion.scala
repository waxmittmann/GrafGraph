//package io.grafgraph.render.crud
//
//import io.grafdefinition.WithBuilders
//import io.grafgraph.render.crud.Util.indent
//
//object RenderVersion {
//
//    def renderVersion(vertex: WithBuilders#Vertex, last: WithBuilders#VertexState): String =
//      s"""
//         |
//         |  case class ByUid(uid: UUID) extends VertexByUid with ${vertex.name}
//         |  case class ByQuery(query: String) extends VertexByQuery with ${vertex.name}
//         |  sealed trait New extends VertexNew[${vertex.name}]
//         |
//         |${indent(2)(last.states.zipWithIndex.map { case (state, index) =>
//        RenderState.renderState(index, last.states.length == 1, vertex, state)
//      }.toList.mkString("\n"))}
//       """.stripMargin
//
//}
