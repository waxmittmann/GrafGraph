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
          .edge("definition", artifactDefn.states.head)
          .attribute(boolean("exists", false))
        .state("Exists")
          .edge("definition", artifactDefn.states.head)
          .attribute(boolean("exists", true))
      .done

    val workflowDefinition: Lake.Vertex =
      vertex("WorkflowDefn")
      .state("Instance")
        .edge("artifactDefinition", artifactDefn.states.head, toMany = true)
        .attribute(Attr.String("definition"))
      .done

    val workflowInstance: Lake.Vertex =
      vertex("WorkflowInstance")
        .state("Running")
          .edge("output", artifact.states.head, toMany = true)
          .edge("definition", workflowDefinition.states.head)
          .attribute(Attr.String("jobUid"))
          .attribute(Attr.String("status", Some("Running")))
        .state("Complete")
          .edge("output", artifact.states.head, toMany = true)
          .edge("definition", workflowDefinition.states.head)
          .attribute(Attr.String("jobUid"))
          .attribute(Attr.String("status"))
      .done

  // Not great
  val allVertices: List[Lake.Vertex] =
    artifactDefn :: artifact :: workflowDefinition :: workflowInstance :: Nil

  val allClazzez: List[Lake.Clazz] =
    artifactClazzDefn :: Nil
}
