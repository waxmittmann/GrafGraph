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
    states: Seq[VertexState]
  ) {
    // Can I tag version to its vertex? like 'self.Version'?
    //      def get(version: Version): VertexVersion = versions.find(_.version == version).get

//    lazy val latest: VertexVersion = versions.last
  }

  object Vertex {
//    def apply(name: String, clazz: Option[Clazz], inititalVersion: VertexVersion): Vertex =
    def apply(name: String, clazz: Option[Clazz], states: Seq[VertexState]): Vertex =
      Vertex(name, clazz, states)
  }

  object VertexState {
    def apply(
      name: String,
      edges: Seq[Edge],
      attributes: Seq[Attribute]
    ): VertexState = {
      new VertexState(name, edges, attributes)
    }
  }

  // todo: case class ok?
  case class VertexState private(
    name: String,
    edges: Seq[Edge],
    instanceAttributes: Seq[Attribute]
  ) {
    def allAttributes: Seq[Attribute] = GlobalAttributes ++ instanceAttributes
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

  sealed trait Edge {
    val name: String
    val optional: Boolean
    val toMany: Boolean
  }

  case class OtherEdge(
    name: String,
    to: Vertex,
    optional: Boolean = false,
    toMany: Boolean = false,
    attribute: Seq[Attribute] = Seq.empty
  ) extends Edge

  case class SelfEdge(
    name: String,
    attribute: Seq[Attribute],
    optional: Boolean = false,
    toMany: Boolean = false
  ) extends Edge

}
