package io.grafgraph.example

import io.grafgraph.example.Lake._
import io.grafgraph.render.{GraphCrudRenderer, GraphDaoRenderer}

object LakeMain {
  def main(args: Array[String]): Unit = {

/*
    val graphDefinition = GraphDaoRenderer.write(
      artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil
    ).cur.reverse.mkString("\n\n")

    println(graphDefinition)
*/


    println(GraphCrudRenderer.render(Lake))

  }

}
