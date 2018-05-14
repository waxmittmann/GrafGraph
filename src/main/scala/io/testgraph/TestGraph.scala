package io.testgraph

import java.util.UUID
import org.neo4j.driver.v1._
import scala.collection.JavaConverters._

class Neo4jGraph(url: String, user: String, password: String) {

  private val token: AuthToken = AuthTokens.basic(user, password)

  val driver: Driver = GraphDatabase.driver(
    url,
    token,
    Config.build.withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig
  )

  def close(): Unit = {
    driver.close()
  }
}

class Graph(graph: Neo4jGraph) {

  sealed trait CreateReadUpdate
  sealed trait VertexByUid extends CreateReadUpdate { val uid: UUID }
  sealed trait VertexByQuery extends CreateReadUpdate { val query: String }
  sealed trait VertexNew[A] extends CreateReadUpdate

  // ${renderMeta(graph.meta)} // Not implemented

  sealed trait Artifact {}

  object ArtifactDefn {
    sealed trait ArtifactDefn

    case class ByUid(uid: UUID) extends VertexByUid with ArtifactDefn
    case class ByQuery(query: String) extends VertexByQuery with ArtifactDefn
    sealed trait New extends VertexNew[ArtifactDefn]

    case class Instance(
        uid: UUID,
        label: String
    ) extends ArtifactDefn
        with New

    def create(ele: Instance): Unit = {

      val session = graph.driver.session()

      session.writeTransaction {
        tx =>

          val params =
            Map[String, Object](
              "uid" -> ele.uid.toString,
              "label" -> ele.label
            ).asJava

          tx.run("""
     CREATE (Instance: Class_Instance {uid: {uid}, label: {label}})
     
            """.stripMargin,
                 params)
          tx.success()
      }
      session.close()

    }
    //def create(newartifactDefn: New): New = ???

  }

  object WorkflowArtifact {
    sealed trait WorkflowArtifact

    case class ByUid(uid: UUID) extends VertexByUid with WorkflowArtifact
    case class ByQuery(query: String)
        extends VertexByQuery
        with WorkflowArtifact
    sealed trait New extends VertexNew[WorkflowArtifact]

    case class Exists(
        uid: UUID,
        exists: Boolean = true,
        definition: ArtifactDefn.ArtifactDefn
    ) extends Artifact
        with WorkflowArtifact
        with New

    def create(ele: Exists): Unit = {

      val session = graph.driver.session()

      session.writeTransaction {
        tx =>

          val params =
            Map[String, Object](
              "uid" -> ele.uid.toString,
              "exists" -> true.booleanValue().asInstanceOf[Object]
            ).asJava

          tx.run("""
     CREATE (Exists: Class_Exists {uid: {uid}, exists: {exists}})
     ???
            """.stripMargin,
                 params)
          tx.success()
      }
      session.close()

    }

    case class Placeholder(
        uid: UUID,
        exists: Boolean = false,
        definition: ArtifactDefn.ArtifactDefn
    ) extends Artifact
        with WorkflowArtifact
        with New

    def create(ele: Placeholder): Unit = {

      val session = graph.driver.session()

      session.writeTransaction {
        tx =>

          val params =
            Map[String, Object](
              "uid" -> ele.uid.toString,
              "exists" -> false.booleanValue().asInstanceOf[Object]
            ).asJava

          tx.run(
            """
     CREATE (Placeholder: Class_Placeholder {uid: {uid}, exists: {exists}})
     ???
            """.stripMargin,
            params)
          tx.success()
      }
      session.close()

    }
    //def create(newworkflowArtifact: New): New = ???

  }

  object WorkflowDefn {
    sealed trait WorkflowDefn

    case class ByUid(uid: UUID) extends VertexByUid with WorkflowDefn
    case class ByQuery(query: String) extends VertexByQuery with WorkflowDefn
    sealed trait New extends VertexNew[WorkflowDefn]

    case class Instance(
        uid: UUID,
        definition: String,
        artifactDefinition: ArtifactDefn.ArtifactDefn
    ) extends WorkflowDefn
        with New

    def create(ele: Instance): Unit = {

      val session = graph.driver.session()

      session.writeTransaction {
        tx =>

          val params =
            Map[String, Object](
              "uid" -> ele.uid.toString,
              "definition" -> ele.definition
            ).asJava

          tx.run(
            """
     CREATE (Instance: Class_Instance {uid: {uid}, definition: {definition}})
     ???
            """.stripMargin,
            params)
          tx.success()
      }
      session.close()

    }
    //def create(newworkflowDefn: New): New = ???

  }

  object WorkflowInstance {
    sealed trait WorkflowInstance

    case class ByUid(uid: UUID) extends VertexByUid with WorkflowInstance
    case class ByQuery(query: String)
        extends VertexByQuery
        with WorkflowInstance
    sealed trait New extends VertexNew[WorkflowInstance]

    case class Complete(
        uid: UUID,
        status: String,
        jobUid: String,
        definition: WorkflowDefn.WorkflowDefn,
        output: WorkflowArtifact.WorkflowArtifact
    ) extends WorkflowInstance
        with New

    def create(ele: Complete): Unit = {

      val session = graph.driver.session()

      session.writeTransaction {
        tx =>

          val params =
            Map[String, Object](
              "uid" -> ele.uid.toString,
              "status" -> ele.status,
              "jobUid" -> ele.jobUid
            ).asJava

          tx.run(
            """
     MATCH (a: A {uid: {a.uid}}
     CREATE (complete: Class_Complete {uid: {uid}, status: {status}, jobUid: {jobUid}})
     (complete)->(a)
     (complete)->(b: B {uid: {b.uid}, some: {b.some}}
       (b)->(c: C {uid: {b.c.uid}, other: {b.c.other} )
       UNROLL b.ds AS b_d
       (b)->(d: D {uid: {b_d.uid}, other: {b_d.other} )
            """.stripMargin,
            params)
          tx.success()
      }
      session.close()
    }

    case class Running(
        uid: UUID,
        status: String = "Running",
        jobUid: String,
        definition: WorkflowDefn.WorkflowDefn,
        output: WorkflowArtifact.WorkflowArtifact
    ) extends WorkflowInstance
        with New

    def create(ele: Running): Unit = {

      val session = graph.driver.session()

      session.writeTransaction {
        tx =>

          val params =
            Map[String, Object](
              "uid" -> ele.uid.toString,
              "status" -> Running,
              "jobUid" -> ele.jobUid
            ).asJava

          tx.run(
            """
     CREATE (Running: Class_Running {uid: {uid}, status: {status}, jobUid: {jobUid}})
     ???
     ???
            """.stripMargin,
            params)
          tx.success()
      }
      session.close()

    }
    //def create(newworkflowInstance: New): New = ???

  }

}
