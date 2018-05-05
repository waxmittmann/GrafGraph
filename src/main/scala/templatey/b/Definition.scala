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
      val edges: Seq[Edge],
      val attributes: Seq[A]
    )

    object VertexDefinition {
      def apply(
        name: Option[String],
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


    object Builders {
      def v(name: String, version: VertexVersion) = Vertex(name, Seq(version))

      def v(name: String, version: Seq[VertexVersion]) = Vertex(name, version)


      case class VertexVersionBuilder3(name: Option[String], edges: Seq[Edge], attributes: Seq[GraphAttribute]) {
        def >>(e: GraphAttribute): VertexVersionBuilder3 = this.copy(attributes = e +: attributes)

        def | : VertexVersion = VertexVersion(VertexDefinition(name, edges, attributes))
      }

      case class VertexVersionBuilder2(name: Option[String], edges: Seq[Edge]) {
        def >>(e: Edge): VertexVersionBuilder2 = this.copy(edges = e +: edges)

        def | : VertexVersionBuilder3 = VertexVersionBuilder3(name, edges, Seq.empty)

        def |(e: GraphAttribute) : VertexVersionBuilder3 = VertexVersionBuilder3(name, edges, Seq(e))
      }

      class VertexVersionBuilder1() {
        def >(name: String): VertexVersionBuilder2 = VertexVersionBuilder2(Some(name), Seq.empty)
        def > : VertexVersionBuilder2 = VertexVersionBuilder2(None, Seq.empty)
      }

//      def vv(name: String): VertexVersionBuilder2 = VertexVersionBuilder2(Some(name), Seq.empty)
//      def vv: VertexVersionBuilder2 = VertexVersionBuilder2(None, Seq.empty)


      def vv: VertexVersionBuilder1 = new VertexVersionBuilder1()
//      def vv: VertexVersionBuilder1 = new VertexVersionBuilder1()

//      def vv(defn: VertexDefinition) = VertexVersion(defn)
//
//      def vv(
//        edges: Seq[Edge],
//        attributes: Seq[A] = Seq.empty
//      ) = VertexVersion(VertexDefinition(None, edges, attributes))
//
//      def vv(
//        name: String,
//        edges: Seq[Edge],
//        attributes: Seq[A] = Seq.empty
//      ) = VertexVersion(VertexDefinition(Some(name), edges, attributes))
//
//      def vv(attributes: Seq[A]) = VertexVersion(VertexDefinition(None, Seq.empty, attributes))
//
//      def vv(name: String, attributes: Seq[A]) = VertexVersion(VertexDefinition(Some(name), Seq.empty, attributes))

//      def vv(
//        name: String,
//        edges: Seq[Edge]
//      ) = VertexVersion(VertexDefinition(Some(name), edges, Seq.empty))

      //
//      def vv(
//        edges: Seq[Edge]
//      ) = VertexVersion(VertexDefinition(None, edges, Seq.empty))

    }

    /*
          vertex workflowInstance
          ---
          definition ArtifactDefn* [
            foo: String
            bar: Int
          ]
          ---
          snoo String
          gloo Int
          ---
     */

//    case class BuildingDefinitions(name: String) {
//      def
//    }

//    case class EdgeBuilder2()
//
//    case class EdgeBuilder1(name: String) {
//
//    }
//
//    case class BuildingDefinitionsPreSeparator(name: String) {
//      def ___(name: String): BuildingDefinitions = {
//        EdgeBuilder1(name)
//      }
//    }
//
//    def vertex(name: String): BuildingDefinitionsPreSeparator =  BuildingDefinitionsPreSeparator(name)

  }
}