package templatey.b

import templatey.b.Definition.Graph
import templatey.b.Lake.graph

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

    def boolean(name: java.lang.String, value: java.lang.Boolean): Attribute = Boolean(name, Some(value))
    def boolean(name: java.lang.String): Attribute = Boolean(name, None)
  }
  import Attr.boolean

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
      import graph.Builders3._

      //      import graph.Builders._


//      val a: graph.Builders.VertexVersionBuilder1 = vv
//      val b = a.>
//      val c = b >> OtherEdge("definition", ???)
//      val d: graph.Builders.VertexVersionBuilder3 = c |
//      val e: graph.VertexVersion = d |


      val artifactDefn =
        vertex("ArtifactDefn")
        .version
        .defn
        .attribute(Attr.String("label"))
        .done

      val artifact =
        vertex("Artifact")
        .version
          .defn("Placeholder")
            .otherEdge("definition", artifactDefn)
            .attribute(boolean("exists", false))
          .defn("Exists")
            .otherEdge("definition", artifactDefn)
            .attribute(boolean("exists", true))
        .done

      val workflowDefinition =
        vertex("WorkflowDefn")
        .version
        .defn
        .otherEdge("artifactDefinition", artifactDefn, toMany = true)
        .attribute(Attr.String("definition"))
        .done

      val workflowInstance =
        vertex("WorkflowInstance")
        .version
          .defn("Running")
            .otherEdge("output", artifact, toMany = true)
            .otherEdge("definition", workflowDefinition)
            .attribute(Attr.String("jobUid"))
            .attribute(Attr.String("status", Some("Running")))
          .defn("Complete")
            .otherEdge("output", artifact, toMany = true)
            .otherEdge("definition", workflowDefinition)
            .attribute(Attr.String("jobUid"))
            .attribute(Attr.String("status"))
        .done

      val graphDefinition = GraphLibraryFactory.write(
        artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil
      ).cur.reverse.mkString("\n\n")

      println(graphDefinition)

    }
  }
}
