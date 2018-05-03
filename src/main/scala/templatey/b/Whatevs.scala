object Graph {

  sealed trait GraphElement {
    val uid: String
  }


  case class ArtifactDefn(
    uid: String,
    label: String,
  ) extends GraphElement


  case class Artifact(
    uid: String,
    exists: Boolean,
    artifactDefn: ArtifactDefn,
  ) extends GraphElement


  case class WorkflowDefn(
    uid: String,
    definition: String,
    artifactDefns: Seq[ArtifactDefn],
  ) extends GraphElement


  case class WorkflowInstance(
    uid: String,
    jobUid: String,
    artifacts: Seq[Artifact],
    workflowDefn: WorkflowDefn,
  ) extends GraphElement


}