package io.grafgraph.example

import io.grafgraph.example.Lake._
import io.grafgraph.render.GraphLibraryFactory

object LakeMain {
  def main(args: Array[String]): Unit = {

    val graphDefinition = GraphLibraryFactory.write(
      artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil
    ).cur.reverse.mkString("\n\n")

    println(graphDefinition)
  }
}
