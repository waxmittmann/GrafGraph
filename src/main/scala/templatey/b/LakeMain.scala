package templatey.b

import Lake._

object LakeMain {
  def main(args: Array[String]): Unit = {

    val graphDefinition = GraphLibraryFactory.write(
      artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil
    ).cur.reverse.mkString("\n\n")

    println(graphDefinition)
  }
}