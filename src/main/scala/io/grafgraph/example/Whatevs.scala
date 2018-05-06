package io.grafgraph.example

object Graph {

  sealed trait GraphElement {
    val uid: String

  }



  case class ArtifactDefn(
    uid: String,
    label: String,


  ) extends GraphElement


  sealed trait Artifact
  case class Artifact_Exists(
    uid: String,
    exists: Boolean= true,

    definition: ArtifactDefn,

  ) extends GraphElement with Artifact



  case class Artifact_Placeholder(
    uid: String,
    exists: Boolean= false,

    definition: ArtifactDefn,

  ) extends GraphElement with Artifact



  case class WorkflowDefn(
    uid: String,
    definition: String,

    artifactDefinition: ArtifactDefn,

  ) extends GraphElement


  sealed trait WorkflowInstance
  case class WorkflowInstance_Complete(
    uid: String,
    status: String,
    jobUid: String,

    definition: WorkflowDefn,
    output: Artifact,

  ) extends GraphElement with WorkflowInstance



  case class WorkflowInstance_Running(
    uid: String,
    status: String= "Running",
    jobUid: String,

    definition: WorkflowDefn,
    output: Artifact,

  ) extends GraphElement with WorkflowInstance


}
