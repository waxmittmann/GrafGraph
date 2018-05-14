package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.Attribute
import io.grafgraph.example.Lake
import io.grafgraph.render.crud.Util.{indent, uncapitalize}
import io.grafgraph.scratch.Graph

object RenderEdge {
  def renderEdgeForDao(prefix: String)(selfName: String)(e: WithBuilders#Edge): String = indent(2)({
      //  def writeEdge(self: GraphDefinition[Attribute]#Vertex, e: GraphDefinition[Attribute]#Edge): String = {

      val edgeName = uncapitalize(e.name)

      val r = e match {
        // Hmm ok, so here I need a concrete instance
        case Lake.OtherEdge(_, to: Lake.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Attribute]) => {

          // Ignore attribute for now
          // don't do optional; use toMany for that
          if (toMany) {
            s"${edgeName}s: Seq[${to.name}.${to.name}]"
          } else {
            s"$edgeName: ${to.name}.${to.name}"
          }

        }

        case Lake.SelfEdge(_, attribute, optional, toMany) => {
          if (toMany) {
            s"${edgeName}s: Seq[$selfName]"
          } else {
            s"$edgeName: $selfName"
          }

        }
      }

      s"$prefix$r"
    })

//  def renderEdgeForCypher(
//    vertex: WithBuilders#Vertex,
//    state: WithBuilders#VertexState
//    )(edge: WithBuilders#Edge): String = {
//    edge match {
//      case Lake.OtherEdge(name, to, optional, toMany, attribute) => "???"
//
//      case Lake.SelfEdge(name, attribute, optional, toMany) => "???"
//    }
//  }
}
