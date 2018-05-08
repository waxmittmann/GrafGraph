package io.grafgraph.example

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.GraphDefinition

sealed trait Attribute {
  val name: String
}

// Todo: keep here?
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

object Lake extends WithBuilders[Attribute] {

  override val GlobalAttributes: Seq[GraphAttribute] =
    Seq(Attr.UID("uid"))

//  type OtherGraph = WithBuilders[Attribute]

  val artifactClazzDefn: Clazz = Clazz("Artifact", Seq.empty, Seq.empty)

  val artifactDefn: Vertex =
//    (v("ArtifactDefn") extendz artifactClazzDefn)
      vertex("ArtifactDefn")
      .version
        .state
          .attribute(Attr.String("label"))
      .done

    val artifact: Lake.Vertex =
//      vertex("Artifact")
      (v("WorkflowArtifact") extendz artifactClazzDefn)
      .version
        .state("Placeholder")
          .otherEdge("definition", artifactDefn)
          .attribute(boolean("exists", false))
        .state("Exists")
          .otherEdge("definition", artifactDefn)
          .attribute(boolean("exists", true))
      .done

    val workflowDefinition: Lake.Vertex =
      vertex("WorkflowDefn")
      .version
      .state
      .otherEdge("artifactDefinition", artifactDefn, toMany = true)
      .attribute(Attr.String("definition"))
      .done

    val workflowInstance: Lake.Vertex =
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

  // Not great
  val allVertices: List[Lake.Vertex] =
    artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil

  val allClazzez: List[Lake.Clazz] =
    artifactClazzDefn :: Nil
}