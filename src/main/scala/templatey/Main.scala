package templatey

import scala.collection.mutable

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

      val artifact =
        v(
          name = "artifact",
          a("key", classOf[String], Required) :: Nil,
          edges = e.insertEdge(Edge(Attributes(), workflowDefinition))
        )

      // As example; notice we don't return the vertex itself, so user can't think it's copying. This could have a copy
      // method though
      val x: workflowInstance.VertexBuilder = workflowInstance.appendEdge(Edge(to = artifact))
      // x.copy; allowed, get a vertex back TODO: should need name

      


      // Bs
      v(
        "f",
        a("jobUid", classOf[String], Required) ::
          a("jobName", classOf[Int], Required) ::
          a("completedAt", classOf[String], Required) ::
          a("status", classOf[String], Required) :: Nil,
        List.empty
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
