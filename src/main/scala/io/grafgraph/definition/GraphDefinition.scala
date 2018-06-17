package io.grafgraph.definition

import java.util.UUID

import cats.data.NonEmptyList
import io.grafgraph.definition.Attr.UID

sealed trait Attribute {
  val name: String
}

object Attr {
  // Value probably to become test
  case class Int(name: java.lang.String, value: Option[scala.Int] = None) extends Attribute
  case class String(name: java.lang.String, value: Option[java.lang.String] = None) extends Attribute
  case class UID(name: java.lang.String) extends Attribute
  //case class UID(name: java.lang.String, value: Option[java.lang.String] = None) extends Attribute
  case class Boolean(name: java.lang.String, value: Option[java.lang.Boolean] = None) extends Attribute

  def boolean(name: java.lang.String, value: java.lang.Boolean): Attribute = Boolean(name, Some(value))
  def boolean(name: java.lang.String): Attribute = Boolean(name, None)
}

trait GraphDefinition {

  final val uidAttribute: UID = UID("uid")

  val ExtraGlobalAttributes: Seq[Attribute]
  lazy final val GlobalAttributes: Seq[Attribute] = {
    assert(!ExtraGlobalAttributes.map(_.name).toSet.contains("uid"))
    uidAttribute +: ExtraGlobalAttributes
  }

  // Todo: Honor clazz
  case class Clazz(
    name: String,
    edges: Seq[Edge],
    attributes: Seq[Attribute]
  )

  case class Vertex(
    name: String,
    clazz: Option[Clazz], // Rules: cannot be self, cannot be anything that causes a circular dependency
//    versions: Seq[VertexVersion]
    states: Seq[VertexState] = Seq.empty
  ) {

    def addState(partial: PartialVertexState): Vertex =
      this.copy(states = partial.done(this) +: states)

    // Can I tag version to its vertex? like 'self.Version'?
    //      def get(version: Version): VertexVersion = versions.find(_.version == version).get

//    lazy val latest: VertexVersion = versions.last
  }

//  object Vertex {
//    def apply(name: String, clazz: Option[Clazz], inititalVersion: VertexVersion): Vertex =
//    def apply(name: String, clazz: Option[Clazz], states: Seq[VertexState]): Vertex =
//      Vertex(name, clazz, states)
//  }

  object VertexState {
    def apply(
      parent: Vertex,
      name: String,
      edges: Seq[Edge],
      attributes: Seq[Attribute]
    ): VertexState = {
      new VertexState(parent, name, edges, attributes)
    }
  }

  // todo: case class ok?
  case class VertexState private(
    parent: Vertex,
    name: String,
    edges: Seq[Edge],
    instanceAttributes: Seq[Attribute]
  ) {
    def allAttributes: Seq[Attribute] = GlobalAttributes ++ instanceAttributes
  }

  case class PartialVertexState(
    name: String,
    edges: Seq[Edge],
    instanceAttributes: Seq[Attribute]
  ) {
    def allAttributes: Seq[Attribute] = GlobalAttributes ++ instanceAttributes

    def done(v: Vertex) = VertexState(v, name, edges, instanceAttributes)
  }

  // todo: just make it a nel
//  case class VertexVersion(
//    states: NonEmptyList[VertexState]
//  ) {
//    def ::(vertexDefinition: VertexState): VertexVersion = this.copy(vertexDefinition :: states)
//  }
//
//  object VertexVersion {
//    def apply(defn: VertexState): VertexVersion = VertexVersion(NonEmptyList.of(defn))
//  }

  case class Edge(
    name: String,
    to: VertexState,
    optional: Boolean = false,
    toMany: Boolean = false,
    attribute: Seq[Attribute] = Seq.empty
  )

  //  sealed trait Edge {
//    val name: String
//    val optional: Boolean
//    val toMany: Boolean
//  }
//
//  case class OtherEdge(
//    name: String,
//    to: VertexState,
//    optional: Boolean = false,
//    toMany: Boolean = false,
//    attribute: Seq[Attribute] = Seq.empty
//  ) extends Edge
//
//  // tODO: REMOVE FOR NOW
//  case class SelfEdge(
//    name: String,
//    attribute: Seq[Attribute],
//    optional: Boolean = false,
//    toMany: Boolean = false
//  ) extends Edge

}
