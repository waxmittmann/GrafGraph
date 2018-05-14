package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.render.crud.Util.indent

object RenderClazz {

  def renderClazz(clazz: WithBuilders#Clazz): String = indent(2)({
    s"""
      |sealed trait ${clazz.name} {
      |${indent(2)(clazz.attributes.map(RenderAttribute.renderAttribute("val")).mkString(",\n"))}
      |${indent(2)(clazz.edges.map(RenderEdge.renderEdgeForDao("val")(clazz.name)).mkString(",\n"))}
      |}
    """.stripMargin
  })

}
