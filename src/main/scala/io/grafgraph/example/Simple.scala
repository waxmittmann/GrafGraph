package io.grafgraph.example

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.{Attr, Attribute}

object Simple extends WithBuilders {
  import Attr.boolean

  // Probably add a 'clean' flag here, set to 'false' for any non-raw input that isn't created via pipeline
  override val ExtraGlobalAttributes: Seq[Attribute] = Seq()

  val cDefn: Vertex =
    vertex("C")
      .state("Instance")
      .attribute(Attr.String("cVal"))
      .done

  val bDefn: Vertex =
    vertex("B")
      .state("Instance")
      .attribute(Attr.String("bVal"))
      .edge("c", cDefn.states.head)
    .done

  val aDefn: Vertex =
    vertex("A")
      .state("Instance")
      .attribute(Attr.String("aVal"))
      .edge("b", bDefn.states.head)
    .done



  // Not great
  val allVertices: List[Simple.Vertex] = Nil

  val allClazzez: List[Simple.Clazz] = Nil
}
