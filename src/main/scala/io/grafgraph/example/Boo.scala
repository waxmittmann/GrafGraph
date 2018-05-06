package io.grafgraph.example

import io.grafgraph.definition.GraphDefinition
import io.grafgraph.example.Lake

object Boo {
  def make[A]: Lake.Vertex =
    Lake.Vertex("a", Lake.VertexVersion(Lake.VertexState(Some("a"), Seq.empty, Seq.empty)))




}
