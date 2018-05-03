package templatey.a

import scala.collection.mutable

/*
  Ok, versioning + everything:

  - we have an 'archetype' or 'class' or 'factory'
  - then we create instances; these are assigned an incremental id per instance
  - instances can be modified when created
  - instances can be migrated / versioned; this creates a new invisible instance with new version number; henceforth (from that
    migration forward) any reference to core instance can also be to any descendant.
    (to make simpler, could be just have a core reference, not be able to reference by version as well)
   so either:

   This one we will when building program have to create a copy of latest and store that inside new vertex.
   When migrate is called,
   ```
    klass Klass { defn }

    vertex initialFoo:Klass { defn }

    migrate initialFoo { defn } // creates initialFoo:2

    migrate backward-compatible initialFoo { defn } // not allowed to delete anything or add any mandatory fields

    vertex referencesInitial {
      ...
      edgeTo initialFoo // will be latest at this point
      edgeTo initialFoo:2 // will be a fixed version
      edgeTo initialFoo:latest // will be latest at this point
      edgeTo initialFoo:any // may be any version
      ...
    }
   ```
   or for second part just this, will allow any version.
   This one we will when building program mean that we build it up with mutable references, all pointing to a vertex,
   and just add versions to that vertex when migrate is called.

    ```
    vertex referencesInitial {
      ...
      edgeTo initialFoo // will be any version OR will be latest version
      ...
     }
    ```

    Now we can do version checking and return DAO case class for our graph objects that are versioned, like:
    ```
    case class InitialFoo.V1.Created(
      ...
    )
    case class InitialFoo.V1.Complete(
      ...
      status: "Complete"
      plus attributes ...
    )


    case class InitialFoo.V2.Created(
      ...
      plus edgeTo blah
    )
    case class InitialFoo.V2.Complete(
      ...
      status: "Complete"
      plus attributes ...
      plus edgeTo blah
    )


    // .get[InitialFoo.V2.Complete]("12e3er4-12e3-1223-122312")

    // 'Any' will be a version that has union of all fields of all non-retired versions, and any that may or may
    not exist will be Option (so anything that doesn't exist from first non-retired version)
    // .get[InitialFoo.Any.Complete]("12e3er4-12e3-1223-122312")

  states, versions, instances
    each instance is an instance of a klazz
    each instance has multiple versions (incl. initial)
      each version has multiple states (created, complete, ...)

    ```


    Could also:
    - retire versions (not gen source or allow)
    - create migration sets (sets of classes with a base version)

    Should also:
    - have behavior for failure to parse to compatible version (ignore, throw, log, ...)
    - some way of ensuring backward compatibility for minor versions, non-compatibility for major versions


 */

object Main {

  class GraphFactory[T] {

    case class VertexStates(states: Vertex)

    case class Edge(attributes: Attributes = Attributes(), to: Vertex)

    class Edges(edges: mutable.MutableList[Edge] = new mutable.MutableList[Edge]()) {
      def getEdges = List(edges)

      def insertEdge(edge: Edge): Edges = {
        edges += edge
        this
      }

      def copy: Edges = {
        val lb = new mutable.MutableList[Edge]()
        lb :+ edges
        new Edges(lb)
      }
    }

    case class Attributes(attributes: Seq[T] = Seq.empty)

    sealed trait GraphElement

    class Vertex(
      val name: String,
      val attributes: Attributes,
      vEdges: Edges
    ) {
      class VertexBuilder() {
        def appendEdge(edge: Edge): VertexBuilder = {
          vEdges.insertEdge(edge)
          this
        }

        def copy: Vertex = new Vertex(name, attributes, vEdges.copy)
      }

      def appendEdge(edge: Edge): VertexBuilder = {
        vEdges.insertEdge(edge)
        new VertexBuilder
      }

      def copy: Vertex = {
        new Vertex(name, attributes, vEdges.copy)
      }

      def edges: Edges = vEdges.copy

//      def e(edge: Edge): Vertex = {
//        insertEdge(edge)
//        this
//      }
    }

    def e: Edges = new Edges(new mutable.MutableList[Edge]())

    def v(
      name: String,
      attributes: Seq[T] = Seq.empty,
//      edges: List[Edge]
//      edges: List[Edge]
      edges: Edges = new Edges()
    ) = new Vertex(
      name,
      Attributes(attributes),
//      new Edges(mutable.MutableList(edges.toArray:_*))
      edges
    )

    def v(
      name: String,
      attributes: Seq[T],
      //      edges: List[Edge]
      edges: List[Edge]
    ) = new Vertex(
      name,
      Attributes(attributes),
        new Edges(mutable.MutableList(edges.toArray:_*))
    )

    //    def e(edge: Edge): Edge = insertEdge(edge)


  }

  case class TargetAttribute(name: String, `type`: Class[_], mod: Mod)
  val targetGraph = new GraphFactory[TargetAttribute] {
    def a(
      name: String,
      `type`: Class[_],
      mod: Mod
    ) = TargetAttribute(name, `type`, mod)
  }

  case class ExistingAttribute(name: String, value: Object)
  val existingGraph = new GraphFactory[ExistingAttribute]


  sealed trait Mod
  case object Required extends Mod
  case object Optional extends Mod


  def main(args: Array[String]): Unit = {

    val targetWorkflowInstance = {
      import targetGraph._

//      Vertex(
//        expectedAttributes = Attributes(List(
//          TargetAttribute("jobUid", classOf[String], Required),
//          TargetAttribute("other1", classOf[Int], Required),
//          TargetAttribute("other2", classOf[String], Optional)
//        )),
//        expectedEdges = Edges(List.empty)
//      )

      val artifact =
        v(
          name = "artifact",
          a("key", classOf[String], Required) ::
            a("type", classOf[String], Required) :: Nil, // Type should be uuid of the type, only 'denormalized' here
          List.empty
        )

      val workflowDefinition =
        v(
          "workflowDefinition",
          a("definition", classOf[String], Required) :: Nil,
          List.empty
        )

      val workflowInstance =
        v(
          "workflowInstance",
          a("jobUid", classOf[String], Required) ::
          a("jobName", classOf[Int], Required) ::
          a("status", classOf[String], Required) :: Nil,
          e.insertEdge(Edge(Attributes(), workflowDefinition))
        )

      /*
          - v:
            - name: "workflowInstance"
            -


          v:workflowInstance
            ---
            "jobUuid", String, Required
            "jobName", String, Required
            ---
            v:artifact, "OUTPUT"
              "timeAt", Datetime, Optional
            ---






          "jobUuid", String, Required
          "jobName", String, Required
          }
          }




       */

      // As example; notice we don't return the vertex itself, so user can't think it's copying. This could have a copy
      // method though
      val x: workflowInstance.VertexBuilder = workflowInstance.appendEdge(Edge(to = artifact))
      // x.copy; allowed, get a vertex back TODO: should need name

      // Bs
      v(
        name = "f",
        attributes =
          a("jobUid", classOf[String], Required) ::
          a("jobName", classOf[Int], Required) ::
          a("completedAt", classOf[String], Required) ::
          a("status", classOf[String], Required) :: Nil,
        edges = List.empty
      )
    }


    val realWorkflowInstance = new existingGraph.Vertex(
      name = "Boo",
      attributes = existingGraph.Attributes(List(
        ExistingAttribute("jobUid", "Super"),
        ExistingAttribute("other1", "One"),
        ExistingAttribute("other3", "TooMuch"),
      )),
      vEdges = new existingGraph.Edges()
    )

    println(checkVertex(targetWorkflowInstance, realWorkflowInstance))
  }

//  def checkVertexStates(targetVertex: targetGraph.Vertex, existingVertex: existingGraph.Vertex): String = {
//
//  }

  def checkVertex(targetVertex: targetGraph.Vertex, existingVertex: existingGraph.Vertex): String = {

    val target = targetVertex.attributes.attributes
      .map(ta => (ta.name, ta)).toMap

    val existing = existingVertex.attributes.attributes
      .map(ta => (ta.name, ta)).toMap


    val targetExisting = (target.keySet ++ existing.keySet).map { key =>
      (target.get(key), existing.get(key))
    }

    val result = targetExisting.map {

      // Todo: Try extractors
      case (Some(target), Some(existing)) => {
        if (!target.`type`.isInstance(existing.value)) {
          Left(s"Wrong class ${target.name}: ${target.`type`} != ${existing.value.getClass}")
        } else
          Right()
      }
      case (Some(target), None) => {
        if (target.mod == Required)
          Left(s"Missing attribute ${target.name}")
        else
          Right()
      }
      case (None, Some(existing)) =>
        Left(s"Existing attribute $existing not declared.")

      case (None, None) => throw new Exception("Impossible, no target or existing")

    }

    val resultStr = result.collect { case Left(err) => err }.mkString("\n")
    resultStr
  }

}
