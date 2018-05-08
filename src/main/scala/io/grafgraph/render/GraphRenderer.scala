//package io.grafgraph.render
//
////import io.grafgraph.example.Lake.graph
//import cats.data.Writer
//import io.grafgraph.example.{Attr, Attribute, Lake}
//
////trait Writer {
////  def write(str: String): Writer
////}
//
//// Couldn't use type projection because I want to match on some case classes, and it won't let me based on the type
//// projection, nor on a `type X = Y#Z` style thing.
//trait GraphRenderer {
//
//  type FullGraphData
//
//  def write(definitions: Seq[Lake.Vertex]): String = {
//    val graphData = calcFullGraphData(definitions)
//
////   val withPreGraph = renderGraphDataPre(graphData)
////      .write(perVertex(graphData, definitions))
//
//
//    s"""
//       |// Pre data START
//       |${renderGraphDataPre(graphData)}
//       |// Pre data END
//       |
//       |// Vertex data START
//       |${renderVertices(graphData, definitions)}
//       |// Vertex data END
//       |
//       |//Post data START
//       |${renderGraphDataPost(graphData)}
//       |// Post data END
//       |
//     """.stripMargin
//  }
//
//  def calcFullGraphData(definitions: Seq[Lake.Vertex]): FullGraphData
//
//  def renderGraphDataPre(graphData: FullGraphData): String
//
//  type FullVertexData
//
//  def renderVertices(graphData: FullGraphData, definitions: Seq[Lake.Vertex]): String = {
//    definitions.map(renderVertex).mkString("\n\n")
//
//  def renderVertex(vertex: Lake.Vertex): String = {
//    val fullVertexData = calcFullVertexData(vertex: Lake.Vertex)
//
//    s"""
//       |${renderOpenVertex(fullVertexData)}
//       |${vertex.versions.map(renderVersion)}
//       |${renderOpenVertex(fullVertexData)}
//       |${renderOpenVertex(fullVertexData)}
//     """.stripMargin
//  }
//
//
//  def renderVersion(version: Lake.VertexVersion): String = {
//    s"""
//       |${version.allowedDefinitions.map(renderState)}
//     """.stripMargin
//  }
//
//  def renderState(state: Lake.VertexState): String  = {
//    state.
//  }
//
//  def calcFullVertexData(vertex: Lake.Vertex): FullVertexData
//
//
//  def renderGraphDataPost(graphData: FullGraphData): String
//
//}
