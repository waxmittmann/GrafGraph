package io.grafgraph.definition

case class Builders[A](graph: GraphDefinition[A]) {
//  import graph._

  def vertex(name: String): VertexBuilder = VertexBuilder(name, Seq.empty)

  case class VertexBuilder(name: String, versions: Seq[graph.VertexVersion]) {
    def version: VersionBuilder = IncompleteVersionBuilder(this)

    def done: graph.Vertex = graph.Vertex(name, versions)
  }

  sealed trait VersionBuilder {
    val parent: VertexBuilder

    def definitions: Seq[graph.VertexState]

    def state(name: String) = StateBuilder(this, Some(name), Seq.empty, Seq.empty)
    def state = StateBuilder(this, None, Seq.empty, Seq.empty)

    def withState(defn: graph.VertexState): CompleteVersionBuilder =
      CompleteVersionBuilder(parent, Seq(defn))
  }

  case class IncompleteVersionBuilder(parent: VertexBuilder) extends VersionBuilder {
    override def definitions: Seq[graph.VertexState] = Seq.empty
  }

  case class CompleteVersionBuilder(parent: VertexBuilder, definitions: Seq[graph.VertexState]) extends VersionBuilder
  case class StateBuilder(
    parent: VersionBuilder,
    name: Option[String],
    edges: Seq[graph.Edge],
    attributes: Seq[graph.GraphAttribute]
  ) {
    def otherEdge(
      name: String,
      to: graph.Vertex,
      attributes: Seq[graph.GraphAttribute] = Seq.empty,
      toMany: Boolean = false,
      optional: Boolean = false
    ): StateBuilder =
      this.copy(edges = graph.OtherEdge(name, to, toMany, optional, attributes) +: edges)

    def selfEdge(
      name: String,
      attributes: Seq[graph.GraphAttribute] = Seq.empty,
      toMany: Boolean = false,
      optional: Boolean = false
    ): StateBuilder =
      this.copy(edges = graph.SelfEdge(name, attributes, toMany, optional) +: edges)

    def attribute(attribute: graph.GraphAttribute): StateBuilder = this.copy(attributes = attribute +: attributes)

    def state(newName: String): StateBuilder = parent.withState(graph.VertexState(name, edges, attributes)).state(newName)
    def state: StateBuilder = parent.withState(graph.VertexState(None, edges, attributes)).state

    def version: VersionBuilder = {
      val grandparent = parent.parent

      val thisDefn = graph.VertexState(name, edges, attributes)
      val thisVertexVersion = graph.VertexVersion(thisDefn +: parent.definitions)

      IncompleteVersionBuilder(grandparent.copy(versions = thisVertexVersion +: grandparent.versions))
    }

    def done: graph.Vertex = {
      val grandparent = parent.parent

      val thisDefn = graph.VertexState(name, edges, attributes)
      val thisVertexVersion = graph.VertexVersion(thisDefn +: parent.definitions)

      grandparent.copy(versions = thisVertexVersion +: grandparent.versions).done
    }
  }
}