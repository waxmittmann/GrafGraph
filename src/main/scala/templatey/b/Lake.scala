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
      import graph.Builders._


//      val a: graph.Builders.VertexVersionBuilder1 = vv
//      val b = a.>
//      val c = b >> OtherEdge("definition", ???)
//      val d: graph.Builders.VertexVersionBuilder3 = c |
//      val e: graph.VertexVersion = d |

      val e: graph.VertexVersion =
//        (v >> OtherEdge("definition", ???) |) |
        (vv > "a" >> OtherEdge("definition", ???) |) |


//      val vva = vv("atr") >>
//        OtherEdge("definition", ???) >>
//        OtherEdge("definition", ???) |
//        Attr.Int("a") >>
//        Attr.Int("b")

//      val vva: graph.Builders.VertexVersionBuilder3 =
//        vv("atr") >>
//          OtherEdge("definition", ???) >>
//          OtherEdge("definition", ???) |
//          (Attr.Int("a")) >>
//          (Attr.Int("b")) |

//      val vva  =
//        (vv("atr") >>
//          OtherEdge("definition", ???) >>
//          OtherEdge("definition", ???) | Attr.Int("a")).>>(Attr.Int("b")).|

      val vva: graph.VertexVersion =
        (vv > "atr" >>
          OtherEdge("definition", ???) >>
          OtherEdge("definition", ???) |
          Attr.Int("a")).>>(Attr.Int("b")) |

      //        Attr.Int("a") >>
//        Attr.Int("b")

//      val x = vva.|

      val artifactDefn = v(
        "ArtifactDefn",
        vv > "watr" >> OtherEdge("definition", artifactDefn) | Attr.Int("a") |



        //        vv(
//          Nil,
//          Attr.String("label") ::
//            Nil
//        )
      )

      val artifact = v(
        "Artifact",
        vv(
          "Placeholder",
          OtherEdge("definition", artifactDefn) :: Nil,
          boolean("exists", false) :: Nil // need to create early, to keep track of its uid; this will be pattern.
        ) :b:
        vv(
          "Exists",
          OtherEdge("definition", artifactDefn) :: Nil,
          boolean("exists", false) :: Nil // need to create early, to keep track of its uid; this will be pattern.
        ) :: Nil
      )

      val workflowDefinition = Vertex(
        "WorkflowDefn",
        vv(
          OtherEdge("artifactDefinition", artifactDefn, toMany = true) :: Nil,
            Attr.String("definition") :: Nil
        )
      )

      /*
        val workflowInstance =
          v("name")
            e("output",  artifact)+ : ToMany ::
            e("input",  artifact) : ToMany ::


          vertex: workflowInstance
          ---
          -[definition]-> artifactDefn *
             foo: String
             bar: Int
          -[blah]-> thing `1`
          ---
          snoo: String
          gloo: Int
          ---


          vertex: workflowInstance
          ---
          definition: ArtifactDefn* [
            foo: String
            bar: Int
          ]
          ---
          snoo: String
          gloo: Int
          ---
       */




      val workflowInstance = Vertex(
        "WorkflowInstance",
        vv(
          "Running",

          OtherEdge("output", artifact, toMany = true) ::
          OtherEdge("definition", workflowDefinition) ::
            Nil,

          Attr.String("jobUid") ::
          Attr.String("status", Some("Running")) ::
            Nil
        ) ::
        vv(
          "Complete",

          OtherEdge("output", artifact, toMany = true) ::
            OtherEdge("definition", workflowDefinition) ::
            Nil,

          Attr.String("jobUid") ::
            Attr.String("status") ::
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
