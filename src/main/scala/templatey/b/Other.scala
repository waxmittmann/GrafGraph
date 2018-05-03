package templatey.b

import templatey.b.Main.Graph

object Other {

  def main(args: Array[String]): Unit = {
    // Real
    {
      sealed trait Attribute {
        val name: String
      }

      object Attr {
        case class Int(name: java.lang.String) extends Attribute
        case class String(name: java.lang.String) extends Attribute
        case class UID(name: java.lang.String) extends Attribute
        case class Boolean(name: java.lang.String) extends Attribute
      }

      val graph = new Graph[Attribute] {}



      import graph._

      val artifactDefn = v(
        "artifactDefn",
        vv(Nil,
          Attr.String("uid") ::
          Attr.String("label") ::
            Nil
        )
      )

      val artifact = v(
        "artifact",
        vv(
          OtherEdge(artifactDefn) ::
            Nil,

          Attr.String("uid") ::
            Attr.Boolean("exists") :: // need to create early, to keep track of its uid; this will be pattern.
            Nil
        )
      )

      val workflowDefinition = Vertex(
        "workflowDefn",
        vv(
          OtherEdge(artifactDefn, toMany = true) :: Nil,
          Attr.String("uid") ::
            Attr.String("definition") :: Nil
        )
      )

      val workflowInstance = Vertex(
        "workflowInstance",
        vv(
          OtherEdge(artifact, toMany = true) ::
          OtherEdge(workflowDefinition) ::
            Nil,

          Attr.String("uid") ::
          Attr.String("jobUid") ::
            Nil
        )
      )
    }
  }
}
