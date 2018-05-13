package io.grafgraph.example

import java.io.File
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import io.grafgraph.render.{GraphCrudRenderer, GraphDaoRenderer}

object LakeMain {
  def main(args: Array[String]): Unit = {

    println(new File(".").getAbsolutePath)
    println(new File("./src/main/scala/io/testgraph").getAbsolutePath)
    println(new File("./src/main/scala/io/testgraph").exists())
    println(GraphCrudRenderer.render(Lake))

    Files.write(
      Paths.get("./src/main/scala/io/testgraph/TestGraph.scala"),
      GraphCrudRenderer.render(Lake).getBytes,
      StandardOpenOption.CREATE
    )
  }

}
