//package io.grafgraph.render
//import io.grafgraph.example.Lake
//
//class GraphCrudRenderer extends GraphRenderer {
//  case class FullCrudData(
//
//  )
//
//  override type FullGraphData = FullCrudData
//
//  override def calcFullGraphData(definitions: Seq[Lake.Vertex]): FullCrudData = {
//    FullCrudData()
//  }
//
//  override def renderGraphDataPre(graphData: FullCrudData): String = {
//
//    "sealed trait GraphCrud"
//
//  }
//
//  override def renderVertex(vertex: Lake.Vertex): String = {
//    s"""
//       |object ${vertex.name}Crud {
//       |
//       |
//       |}
//     """.stripMargin
//
//
//  }
//
//  override def renderGraphDataPost(graphData: FullCrudData): String = ""
//}
