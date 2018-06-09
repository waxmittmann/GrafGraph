package io.grafdefinition

import cats.data.NonEmptyList
import io.grafgraph.definition.{Attribute, GraphDefinition}

// Don't love 'WithBuilders' extending 'GraphDefinition' but it works, and at least this way I can keep apart the
// factory from defn. Still open question as to whether this is the best way to separate, and if it is if it's worth it.

//case class Builders[A](graph: GraphDefinition[A]) {
trait WithBuilders extends GraphDefinition {

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
//    versions: Seq[VertexVersion] = Seq.empty
    states: Seq[VertexState] = Seq.empty
  ) {
//    def version: VersionBuilder = VersionBuilder(this, Seq.empty)
    def state(stateName: String): StateBuilder =
      StateBuilder(this, stateName, Seq.empty, Seq.empty)

//    def done: Vertex = Vertex(name, clazz, versions)
    def done: Vertex = Vertex(name, clazz, states)
  }
//
//  case class VersionBuilder(parent: VertexBuilder2, definitions: Seq[VertexState]) {
//
//    def state(name: String) = StateBuilder(this, name, Seq.empty, Seq.empty)
//
//    def singleState = StateBuilder(this, "Instance", Seq.empty, Seq.empty) // todo: something to ensure they don't add more states
//
//    def withState(defn: VertexState): VersionBuilder =
//      this.copy(definitions = defn +: definitions)
//  }

  case class StateBuilder(
    parent: VertexBuilder2,
    name: String,
    edges: Seq[Edge],
    attributes: Seq[Attribute]
  ) {
    def otherEdge(
      name: String,
      to: Vertex,
      attributes: Seq[Attribute] = Seq.empty,
      toMany: Boolean = false,
      optional: Boolean = false
    ): StateBuilder =
      this.copy(edges = OtherEdge(name, to, toMany, optional, attributes) +: edges)

    def selfEdge(
      name: String,
      attributes: Seq[Attribute] = Seq.empty,
      toMany: Boolean = false,
      optional: Boolean = false
    ): StateBuilder =
      this.copy(edges = SelfEdge(name, attributes, toMany, optional) +: edges)

    def attribute(attribute: Attribute): StateBuilder = this.copy(attributes = attribute +: attributes)

    def state(newName: String): StateBuilder = {
      val newP = parent.copy(states = VertexState(name, edges, attributes) +: parent.states)

      StateBuilder(newP, newName, Seq.empty, Seq.empty)
    }//.state(newName)

    def done: Vertex = {
//      val grandparent = parent.parent

      val thisDefn = VertexState(name, edges, attributes)
//      val thisVertexVersion = VertexVersion(NonEmptyList.of(thisDefn, parent.definitions:_*))

      parent.copy(states = thisDefn +: parent.states)


//      Vertex(
//        name,
//        None, // Hmm?
//        thisDefn +: parent.states
//      )

      //grandparent.copy(versions = thisVertexVersion +: grandparent.versions).done
    }
  }
}