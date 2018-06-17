package io.grafgraph.example

import java.io.File
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import io.grafgraph.render.GraphDaoRenderer
import io.grafgraph.render.crud.GraphCrudRenderer

object LakeMain {
  def main(args: Array[String]): Unit = {

    println(new File(".").getAbsolutePath)
    println(new File("./src/main/scala/io/testgraph").getAbsolutePath)
    println(new File("./src/main/scala/io/testgraph").exists())
    println("Rendered:\n" + GraphCrudRenderer.render(Simple))

    Files.write(
      Paths.get("./src/main/scala/io/testgraph/TestGraph.scala"),
      GraphCrudRenderer.render(Simple).getBytes,
      StandardOpenOption.CREATE
    )
  }

}
