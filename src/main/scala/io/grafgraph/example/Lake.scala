package io.grafgraph.example

import io.grafdefinition.WithBuilders

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

  type OtherGraph = WithBuilders[Attribute]

  val graph: OtherGraph = new WithBuilders[Attribute] {
    override val GlobalAttributes: Seq[GraphAttribute] =
      Seq(Attr.UID("uid"))
  }


//  val builders: Builders[Attribute] = Builders(graph)
//  import builders._
  import graph._

//  import Lake.graph.builders._

//    val artifactDefn: builders.graph.Vertex =
    val artifactDefn: graph.Vertex =
      graph.vertex("ArtifactDefn")
      .version
        .state
          .attribute(Attr.String("label"))
      .done

    val artifact =
      vertex("Artifact")
      .version
        .state("Placeholder")
          .otherEdge("definition", artifactDefn)
          .attribute(boolean("exists", false))
        .state("Exists")
          .otherEdge("definition", artifactDefn)
          .attribute(boolean("exists", true))
      .done

    val workflowDefinition =
      vertex("WorkflowDefn")
      .version
      .state
      .otherEdge("artifactDefinition", artifactDefn, toMany = true)
      .attribute(Attr.String("definition"))
      .done

    val workflowInstance =
      vertex("WorkflowInstance")
      .version
        .state("Running")
          .otherEdge("output", artifact, toMany = true)
          .otherEdge("definition", workflowDefinition)
          .attribute(Attr.String("jobUid"))
          .attribute(Attr.String("status", Some("Running")))
        .state("Complete")
          .otherEdge("output", artifact, toMany = true)
          .otherEdge("definition", workflowDefinition)
          .attribute(Attr.String("jobUid"))
          .attribute(Attr.String("status"))
      .done
}
