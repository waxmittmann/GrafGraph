package templatey.b

import scala.collection.mutable

import com.sun.javafx.geom.Edge


object Main {
  trait Graph[A] {

    type GraphAttribute = A

    val GlobalAttributes: Seq[GraphAttribute] = Seq.empty

    case class Vertex(
      name: String,
      versions: Seq[VertexVersion]
    ) {
      // Can I tag version to its vertex? like 'self.Version'?
      //      def get(version: Version): VertexVersion = versions.find(_.version == version).get

      lazy val latest: VertexVersion = versions.last
    }

    object Vertex {
      def apply(name: String, inititalVersion: VertexVersion): Vertex =
        Vertex(name, inititalVersion :: Nil)
    }

    class VertexDefinition private(
      val edges: Seq[Edge],
      val attributes: Seq[A]
    )

    object VertexDefinition {
      def apply(
        edges: Seq[Edge],
        attributes: Seq[A]
      ): VertexDefinition = {
        new VertexDefinition(edges, GlobalAttributes ++ attributes)
      }
    }

    case class VertexVersion(
      allowedDefinitions: Seq[VertexDefinition]
    ) {
      def ::(vertexDefinition: VertexDefinition): VertexVersion = this.copy(vertexDefinition +: allowedDefinitions)
    }

    object VertexVersion {
      def apply(defn: VertexDefinition): VertexVersion = VertexVersion(Seq(defn))
    }


    //
    //  class Vertex(
    //    edges: Seq[Edge]
    //  )

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

    //  def otherEdge(to: Vertex, attribute: Seq[Attribute] = Seq.empty): Edge = OtherEdge(to, attribute)


    def v(name: String, version: VertexVersion) = Vertex(name, Seq(version))
    def v(name: String, version: Seq[VertexVersion]) = Vertex(name, version)

    def vv(defn: VertexDefinition) = VertexVersion(defn)

    def vv(
      edges: Seq[Edge],
      attributes: Seq[A] = Seq.empty
    ) = VertexVersion(VertexDefinition(edges, attributes))

    def vv(attributes: Seq[A]) = VertexVersion(VertexDefinition(Seq.empty, attributes))

  }
}