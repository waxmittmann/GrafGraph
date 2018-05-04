package templatey.b

import templatey.b.Main.Graph

object Lake {

  sealed trait Attribute {
    val name: String
  }

  object Attr {
    // Value probably to become test
    case class Int(name: java.lang.String, value: Option[scala.Int] = None) extends Attribute
    case class String(name: java.lang.String, value: Option[java.lang.String] = None) extends Attribute
    case class UID(name: java.lang.String, value: Option[java.lang.String] = None) extends Attribute
    case class Boolean(name: java.lang.String, value: Option[java.lang.Boolean] = None) extends Attribute
  }

//  case object OtherGraph extends Graph[Attribute]
  type OtherGraph = Graph[Attribute]

  val graph: OtherGraph = new Graph[Attribute] {
    override val GlobalAttributes: Seq[GraphAttribute] =
      Seq(Attr.UID("uid"))
  }

  def main(args: Array[String]): Unit = {
    // Real
    {

      import graph._

      val artifactDefn = v(
        "ArtifactDefn",
        vv(Nil,
          Attr.String("label") ::
            Nil
        )
      )

      val artifact = v(
        "Artifact",
        vv(
          OtherEdge("definition", artifactDefn) ::
            Nil,

          Attr.Boolean("exists") :: // need to create early, to keep track of its uid; this will be pattern.
            Nil
        )
          :: Nil
      )

      val workflowDefinition = Vertex(
        "WorkflowDefn",
        vv(
          OtherEdge("artifactDefinition", artifactDefn, toMany = true) :: Nil,
            Attr.String("definition") :: Nil
        )
      )

      val workflowInstance = Vertex(
        "WorkflowInstance",
        vv(
          OtherEdge("output", artifact, toMany = true) ::
          OtherEdge("definition", workflowDefinition) ::
            Nil,

          Attr.String("jobUid") ::
          Attr.String("status", Some("Running")) ::
            Nil
        ) ::
        vv(
          OtherEdge("output", artifact, toMany = true) ::
            OtherEdge("definition", workflowDefinition) ::
            Nil,

          Attr.String("jobUid") ::
            Attr.String("status", Some("Succeeded")) ::
            Nil
        ) :: Nil

      )

      val graphDefinition = GraphLibraryFactory.write(
        artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil
      ).cur.reverse.mkString("\n\n")

      println(graphDefinition)

    }
  }
}
