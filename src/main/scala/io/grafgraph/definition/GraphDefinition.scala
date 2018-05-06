package io.grafgraph.definition

import cats.data.NonEmptyList
//import io.grafgraph.definition.GraphDefinition.{Edge, VertexState}

//trait GraphDefinition[A] {
////  type GraphAttribute = A
////
////  val GlobalAttributes: Seq[GraphAttribute] = Seq.empty
////
////  object VertexState {
////    def apply(
////      name: Option[String],
////      edges: Seq[Edge],
////      attributes: Seq[A]
////    ): VertexState = {
////      new VertexState(name, edges, GlobalAttributes ++ attributes)
////    }
////  }
//
//  type Vertex = GraphDefinition.Vertex[A]
//  type VertexState = GraphDefinition.VertexState[A]
//  type VertexVersion = GraphDefinition.VertexVersion[A]
//  type Edge = GraphDefinition.Edge
//  type OtherEdge = GraphDefinition.OtherEdge[A]
//  type SelfEdge = GraphDefinition.SelfEdge[A]
//
//}


trait GraphDefinition[A] {

  type GraphAttribute = A

  val GlobalAttributes: Seq[GraphAttribute] = Seq.empty

  object VertexState {
    def apply(
      name: Option[String],
      edges: Seq[Edge],
      attributes: Seq[A]
    ): VertexState = {
      new VertexState(name, edges, GlobalAttributes ++ attributes)
    }
  }

  // type DTS = DepType#Something

//  val builder: Builders[A] = new Builders[A](this)


//  val v: Vertex = builder.graph.Vertex



//  val x: graph.Vertex = graph.graph.Vertex

//    Graph#Vertex

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
