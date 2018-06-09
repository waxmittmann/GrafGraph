package io.grafgraph.example

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.{Attr, Attribute}

object Lake extends WithBuilders {
  import Attr.boolean

  // Probably add a 'clean' flag here, set to 'false' for any non-raw input that isn't created via pipeline
  override val ExtraGlobalAttributes: Seq[Attribute] = Seq()

  val artifactClazzDefn: Clazz = Clazz("Artifact", Seq.empty, Seq.empty)

  val artifactDefn: Vertex =
//    (v("ArtifactDefn") extendz artifactClazzDefn)
      vertex("ArtifactDefn")
        .state("Instance")
        .attribute(Attr.String("label"))
      .done

    val artifact: Lake.Vertex =
//      vertex("Artifact")
      (v("WorkflowArtifact") extendz artifactClazzDefn)
        .state("Placeholder")
          .otherEdge("definition", artifactDefn)
          .attribute(boolean("exists", false))
        .state("Exists")
          .otherEdge("definition", artifactDefn)
          .attribute(boolean("exists", true))
      .done

    val workflowDefinition: Lake.Vertex =
      vertex("WorkflowDefn")
      .state("Instance")
        .otherEdge("artifactDefinition", artifactDefn, toMany = true)
        .attribute(Attr.String("definition"))
      .done

    val workflowInstance: Lake.Vertex =
      vertex("WorkflowInstance")
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
