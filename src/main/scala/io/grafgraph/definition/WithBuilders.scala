package io.grafdefinition

import cats.data.NonEmptyList
import io.grafgraph.definition.GraphDefinition

// Don't love 'WithBuilders' extending 'GraphDefinition' but it works, and at least this way I can keep apart the
// factory from defn. Still open question as to whether this is the best way to separate, and if it is if it's worth it.

//case class Builders[A](graph: GraphDefinition[A]) {
trait WithBuilders[A] extends GraphDefinition[A] {
//  val graph: GraphDefinition[A]
//case class Builders[A](graph: GraphDefinition[A]) {

//  import _

  def vertex(name: String): VertexBuilder = VertexBuilder(name, Seq.empty)

  case class VertexBuilder(name: String, versions: Seq[VertexVersion]) {
    def version: VersionBuilder = IncompleteVersionBuilder(this)

//    def done: Vertex = Vertex(name, versions)
    def done: Vertex = Vertex(name, versions)
  }

  sealed trait VersionBuilder {
    val parent: VertexBuilder

    def definitions: Seq[VertexState]

    def state(name: String) = StateBuilder(this, Some(name), Seq.empty, Seq.empty)
    def state = StateBuilder(this, None, Seq.empty, Seq.empty)

    def withState(defn: VertexState): CompleteVersionBuilder =
      CompleteVersionBuilder(parent, Seq(defn))
  }

  case class IncompleteVersionBuilder(parent: VertexBuilder) extends VersionBuilder {
    override def definitions: Seq[VertexState] = Seq.empty
  }

  case class CompleteVersionBuilder(parent: VertexBuilder, definitions: Seq[VertexState]) extends VersionBuilder
  case class StateBuilder(
    parent: VersionBuilder,
    name: Option[String],
    edges: Seq[Edge],
    attributes: Seq[GraphAttribute]
  ) {
    def otherEdge(
      name: String,
      to: Vertex,
      attributes: Seq[GraphAttribute] = Seq.empty,
      toMany: Boolean = false,
      optional: Boolean = false
    ): StateBuilder =
      this.copy(edges = OtherEdge(name, to, toMany, optional, attributes) +: edges)

    def selfEdge(
      name: String,
      attributes: Seq[GraphAttribute] = Seq.empty,
      toMany: Boolean = false,
      optional: Boolean = false
    ): StateBuilder =
      this.copy(edges = SelfEdge(name, attributes, toMany, optional) +: edges)

    def attribute(attribute: GraphAttribute): StateBuilder = this.copy(attributes = attribute +: attributes)

    def state(newName: String): StateBuilder = parent.withState(VertexState(name, edges, attributes)).state(newName)
    def state: StateBuilder = parent.withState(VertexState(None, edges, attributes)).state

    def version: VersionBuilder = {
      val grandparent = parent.parent

      val thisDefn: VertexState = VertexState(name, edges, attributes)
      val thisVertexVersion = VertexVersion(NonEmptyList.of(thisDefn, parent.definitions:_*))

      IncompleteVersionBuilder(grandparent.copy(versions = thisVertexVersion +: grandparent.versions))
    }

    def done: Vertex = {
      val grandparent = parent.parent

      val thisDefn = VertexState(name, edges, attributes)
      val thisVertexVersion = VertexVersion(NonEmptyList.of(thisDefn, parent.definitions:_*))

      grandparent.copy(versions = thisVertexVersion +: grandparent.versions).done
    }
  }
}