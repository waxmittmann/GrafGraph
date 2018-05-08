package io.grafgraph.definition

import cats.data.NonEmptyList

trait GraphDefinition[A] {

  type GraphAttribute = A

  val GlobalAttributes: Seq[GraphAttribute] = Seq.empty

  // Todo: Honor clazz
  case class Clazz(
    name: String,
    edges: Seq[Edge],
    attributes: Seq[A]
  )

  case class Vertex(
    name: String,
    clazz: Option[Clazz], // Rules: cannot be self, cannot be anything that causes a circular dependency
    versions: Seq[VertexVersion]
  ) {
    // Can I tag version to its vertex? like 'self.Version'?
    //      def get(version: Version): VertexVersion = versions.find(_.version == version).get

    lazy val latest: VertexVersion = versions.last
  }

  object Vertex {
    def apply(name: String, clazz: Option[Clazz], inititalVersion: VertexVersion): Vertex =
      Vertex(name, clazz, inititalVersion :: Nil)
  }

  object VertexState {
    def apply(
      name: Option[String],
      edges: Seq[Edge],
      attributes: Seq[A]
    ): VertexState = {
      new VertexState(name, edges, GlobalAttributes ++ attributes)
    }
  }

  // todo: case class ok?
  case class VertexState private(
    name: Option[String],
    edges: Seq[Edge],
    attributes: Seq[A]
  )

  // todo: just make it a nel
  case class VertexVersion(
    allowedDefinitions: NonEmptyList[VertexState]
  ) {
    def ::(vertexDefinition: VertexState): VertexVersion = this.copy(vertexDefinition :: allowedDefinitions)
  }

  object VertexVersion {
    def apply(defn: VertexState): VertexVersion = VertexVersion(NonEmptyList.of(defn))
  }

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
    attribute: Seq[A] = Seq.empty
  ) extends Edge

  case class SelfEdge(
    name: String,
    attribute: Seq[A],
    optional: Boolean = false,
    toMany: Boolean = false
  ) extends Edge

//  val builders: Builders[A] = Builders[A](this)
}