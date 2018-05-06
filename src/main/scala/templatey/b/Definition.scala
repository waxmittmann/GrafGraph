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

//        def withDefn(version: VertexDefinition): CompleteVersionBuilder
        def withDefn(defn: VertexDefinition): CompleteVersionBuilder =
          CompleteVersionBuilder(parent, Seq(defn))
      }

      case class IncompleteVersionBuilder(parent: VertexBuilder) extends VersionBuilder {
//        def defn(name: String) = DefnBuilder(this, Some(name), Seq.empty, Seq.empty)
//        def defn = DefnBuilder(this, None, Seq.empty, Seq.empty)

//        override def withDefn(defn: VertexDefinition): CompleteVersionBuilder =
//          CompleteVersionBuilder(parent, Seq(defn))

        override def definitions: Seq[VertexDefinition] = Seq.empty
      }

      case class CompleteVersionBuilder(parent: VertexBuilder, definitions: Seq[VertexDefinition]) extends VersionBuilder  {
//        def defn(name: String) = DefnBuilder(this, Some(name), Seq.empty, Seq.empty)
//        def defn = DefnBuilder(this, None, Seq.empty, Seq.empty)

//        override def withDefn(defn: VertexDefinition): CompleteVersionBuilder =
//          CompleteVersionBuilder(parent, Seq(defn))

//        def version: VersionBuilder = {
//          IncompleteVersionBuilder(parent.copy(versions = VertexVersion(definitions) +: parent.versions))
//        }

//        def done: VertexBuilder = parent.copy(versions = VertexVersion(definitions) +: parent.versions)
      }

      case class DefnBuilder(
        parent: VersionBuilder,
        name: Option[String],
        edges: Seq[Edge],
        attributes: Seq[GraphAttribute]
      ) {
        def edge(edge: Edge): DefnBuilder = this.copy(edges = edge +: edges)

        def attribute(attribute: GraphAttribute): DefnBuilder = this.copy(attributes = attribute +: attributes)

//        def defn(name: Option[String]): DefnBuilder = parent.withDefn(VertexDefinition(name, edges, attributes)).defn(name)
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

        //        def done: CompleteVersionBuilder = parent.withDefn(VertexDefinition(name, edges, attributes))
      }
    }


    object Builders2 {

      // Version builder

      case class VertexBuilder1(name: String, versions: Seq[VertexVersion]) {
        def version(version: VertexVersion): VertexBuilder1 = this.copy(versions = version +: versions)

        def done: Vertex = Vertex(name, versions)
      }

      def vertex(name: String): VertexBuilder1 = VertexBuilder1(name, Seq.empty)

      // VertexVersion builder
      case class VertexVersionBuilder1(
        versions: Seq[VertexDefinition]
      ) {
        def defn(version: VertexDefinition): VertexVersionBuilder1 = this.copy(version +: versions)

        def done = VertexVersion(versions)
      }

//      def vertexVersion(): VertexVersionBuilder1 = VertexVersionBuilder1(Seq.empty)

      // VertexDefn builder
      case class VertexDefnBuilder2(name: Option[String], edges: Seq[Edge], attr: Seq[GraphAttribute]) {
        def attr(a: GraphAttribute) = VertexDefnBuilder2(name, edges, a +: attr)

        def end = VertexDefinition(name, edges, attr)
      }

      case class VertexDefnBuilder1(name: Option[String], edges: Seq[Edge]) {
        def edge(e: Edge): VertexDefnBuilder1 = this.copy(edges = e +: edges)

        def attr(a: GraphAttribute) = VertexDefnBuilder2(name, edges, Seq.empty)
      }

      def vertexDefn(name: String): VertexDefnBuilder1 = {
        VertexDefnBuilder1(Some(name), Seq.empty)
      }

      def vertexDefn(): VertexDefnBuilder1 = {
        VertexDefnBuilder1(None, Seq.empty)
      }



    }


    object Builders {
      def v(name: String, version: VertexVersion) = Vertex(name, Seq(version))

      def v(name: String, version: Seq[VertexVersion]) = Vertex(name, version)


      case class VertexVersionBuilder3(name: Option[String], edges: Seq[Edge], attributes: Seq[GraphAttribute]) {
        def >>(e: GraphAttribute): VertexVersionBuilder3 = this.copy(attributes = e +: attributes)

        def | : VertexVersion = VertexVersion(VertexDefinition(name, edges, attributes))
      }

      case class VertexVersionBuilder2(name: Option[String], edges: Seq[Edge]) {
        def >>(e: Edge): VertexVersionBuilder2 = this.copy(edges = e +: edges)

        def ! : VertexVersionBuilder3 = VertexVersionBuilder3(name, edges, Seq.empty)

        def !(e: GraphAttribute) : VertexVersionBuilder3 = VertexVersionBuilder3(name, edges, Seq(e))
      }

      class VertexVersionBuilder1() {
        def >(name: String): VertexVersionBuilder2 = VertexVersionBuilder2(Some(name), Seq.empty)
        def > : VertexVersionBuilder2 = VertexVersionBuilder2(None, Seq.empty)
      }

      def vv2(name: String): VertexVersionBuilder2 = VertexVersionBuilder2(Some(name), Seq.empty)
      def vv2: VertexVersionBuilder2 = VertexVersionBuilder2(None, Seq.empty)


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