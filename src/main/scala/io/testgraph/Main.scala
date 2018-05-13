//package io.testgraph
//
//import java.util.UUID
//
//object Main {
//
//  def main(args: Array[String]): Unit = {
//
//    val graphClient = new Neo4jGraph("bolt://127.0.0.1:7687", "neo4j", "test")
//
//    val graph = new Graph(graphClient)
//
//    val artifactDefn = graph.ArtifactDefn.Instance(
//      UUID.randomUUID(),
//      "labelA"
//    )
//
//    graph.ArtifactDefn.create(artifactDefn)
//
//    graphClient.close()
//  }
//
//}
