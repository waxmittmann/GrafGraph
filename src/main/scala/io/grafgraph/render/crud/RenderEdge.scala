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

      val r =
        // Hmm ok, so here I need a concrete instance

          // Ignore attribute for now
          // don't do optional; use toMany for that
          if (e.toMany) {
            s"${edgeName}s: Seq[${e.to.name}.${e.to.name}]"
          } else {
            s"$edgeName: ${e.to.name}.${e.to.name}"
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
