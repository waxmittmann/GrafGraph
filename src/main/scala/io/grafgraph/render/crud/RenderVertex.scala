package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.render.crud.Util.{indent, uncapitalize}

object RenderVertex {

    def renderVertex(vertex: WithBuilders#Vertex): String = {
      indent(2)({
        s"""
           |object ${vertex.name} {
           |  sealed trait ${vertex.name}

           |${indent(2)(RenderVersion.renderVersion(vertex, vertex.versions.head))}
           |
           |
           |  //def create(new${uncapitalize(vertex.name)}: New): New = ???
           |
           |}
      """.stripMargin
      })
    }

}
