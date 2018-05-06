package templatey.b

object Definition {

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
      val name: Option[String],
      val edges: Seq[Edge],
      val attributes: Seq[A]
    )

    object VertexDefinition {
      def apply(
        name: Option[String],
        edges: Seq[Edge],
        attributes: Seq[A]
      ): VertexDefinition = {
        new VertexDefinition(name, edges, GlobalAttributes ++ attributes)
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

    object Builders3 {
      def vertex(name: String): VertexBuilder = VertexBuilder(name, Seq.empty)

      case class VertexBuilder(name: String, versions: Seq[VertexVersion]) {
        def version: VersionBuilder = IncompleteVersionBuilder(this)

        def done: Vertex = Vertex(name, versions)
      }

      sealed trait VersionBuilder {
        val parent: VertexBuilder

        def definitions: Seq[VertexDefinition]

        def defn(name: String) = DefnBuilder(this, Some(name), Seq.empty, Seq.empty)
        def defn = DefnBuilder(this, None, Seq.empty, Seq.empty)

        def withDefn(defn: VertexDefinition): CompleteVersionBuilder =
          CompleteVersionBuilder(parent, Seq(defn))
      }

      case class IncompleteVersionBuilder(parent: VertexBuilder) extends VersionBuilder {
        override def definitions: Seq[VertexDefinition] = Seq.empty
      }

      case class CompleteVersionBuilder(parent: VertexBuilder, definitions: Seq[VertexDefinition]) extends VersionBuilder
      case class DefnBuilder(
        parent: VersionBuilder,
        name: Option[String],
        edges: Seq[Edge],
        attributes: Seq[GraphAttribute]
      ) {
        def otherEdge(name: String, to: Vertex, attributes: Seq[GraphAttribute] = Seq.empty, toMany: Boolean = false, optional: Boolean = false): DefnBuilder =
          this.copy(edges = OtherEdge(name, to, toMany, optional, attributes) +: edges)

        def selfEdge(name: String, attributes: Seq[GraphAttribute] = Seq.empty, toMany: Boolean = false, optional: Boolean = false): DefnBuilder =
          this.copy(edges = SelfEdge(name, attributes, toMany, optional) +: edges)

        def attribute(attribute: GraphAttribute): DefnBuilder = this.copy(attributes = attribute +: attributes)

        def defn(name: String): DefnBuilder = parent.withDefn(VertexDefinition(Some(name), edges, attributes)).defn(name)
        def defn: DefnBuilder = parent.withDefn(VertexDefinition(name, edges, attributes)).defn

        def version: VersionBuilder = {
          val grandparent = parent.parent

          val thisDefn = VertexDefinition(name, edges, attributes)
          val thisVertexVersion = VertexVersion(thisDefn +: parent.definitions)

          IncompleteVersionBuilder(grandparent.copy(versions = thisVertexVersion +: grandparent.versions))
        }

        def done: Vertex = {
          val grandparent = parent.parent

          val thisDefn = VertexDefinition(name, edges, attributes)
          val thisVertexVersion = VertexVersion(thisDefn +: parent.definitions)

          grandparent.copy(versions = thisVertexVersion +: grandparent.versions).done
        }
      }
    }
  }
}