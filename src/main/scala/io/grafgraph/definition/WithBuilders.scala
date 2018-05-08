package io.grafdefinition

import cats.data.NonEmptyList
import io.grafgraph.definition.GraphDefinition

// Don't love 'WithBuilders' extending 'GraphDefinition' but it works, and at least this way I can keep apart the
// factory from defn. Still open question as to whether this is the best way to separate, and if it is if it's worth it.

//case class Builders[A](graph: GraphDefinition[A]) {
trait WithBuilders[A] extends GraphDefinition[A] {

//  def clazz(name: String): ClazzBuilder1 = ClazzBuilder1(
//
//  )


  val allVertices: List[Vertex] // Not great :(
  val allClazzez: List[Clazz] // Not great :(

  def vertex(name: String): VertexBuilder2 = VertexBuilder2(
    name,
    None,
    Seq.empty
  )

  def v(name: String): VertexBuilder1 = new VertexBuilder1(name)

  class VertexBuilder1(name: String) {
    def extendz(clazz: Clazz): VertexBuilder2 = VertexBuilder2(name, Some(clazz), Seq.empty)
  }

  case class VertexBuilder2(
    name: String,
    clazz: Option[Clazz],
    versions: Seq[VertexVersion] = Seq.empty
  ) {
    def version: VersionBuilder = VersionBuilder(this, Seq.empty)

    def done: Vertex = Vertex(name, clazz, versions)
  }
  case class VersionBuilder(parent: VertexBuilder2, definitions: Seq[VertexState]) {

    def state(name: String) = StateBuilder(this, Some(name), Seq.empty, Seq.empty)
    def state = StateBuilder(this, None, Seq.empty, Seq.empty)

    def withState(defn: VertexState): VersionBuilder =
      this.copy(definitions = defn +: definitions)
  }

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

    def state(newName: String): StateBuilder = parent.copy().withState(VertexState(name, edges, attributes)).state(newName)
    def state: StateBuilder = parent.withState(VertexState(None, edges, attributes)).state

    def version: VersionBuilder = {
      val grandparent = parent.parent

      val thisDefn: VertexState = VertexState(name, edges, attributes)
      val thisVertexVersion = VertexVersion(NonEmptyList.of(thisDefn, parent.definitions:_*))

      VersionBuilder(grandparent.copy(versions = thisVertexVersion +: grandparent.versions), Seq.empty)
    }

    def done: Vertex = {
      val grandparent = parent.parent

      val thisDefn = VertexState(name, edges, attributes)
      val thisVertexVersion = VertexVersion(NonEmptyList.of(thisDefn, parent.definitions:_*))

      grandparent.copy(versions = thisVertexVersion +: grandparent.versions).done
    }
  }
}