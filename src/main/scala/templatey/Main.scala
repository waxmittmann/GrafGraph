package templatey

object Main {

  class GraphFactory[T] {

    case class VertexStates(states: Vertex)

    case class Edge(attributes: T)

    case class Edges(edges: List[Edge])

    case class Attributes(attributes: Seq[T])

    sealed trait GraphElement

    case class Vertex(
      attributes: Attributes,
      edges: Edges
    )

    def v(
      attributes: Seq[T],
      edges: List[Edge]
    ) = Vertex(
      Attributes(attributes),
      Edges(edges)
    )

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

    /*
        val targetWorkflowInstance = v(
          a("jobUid", `string`, Required) +
          a("other1", , `int`) +
          a("other2", `string`, Optional),

          noEdges
        )

     */


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

      v(
        a("jobUid", classOf[String], Required) ::
        a("jobName", classOf[Int], Required) ::
        a("status", classOf[String], Required) :: Nil,

        List.empty
      )

      v(
        a("jobUid", classOf[String], Required) ::
          a("jobName", classOf[Int], Required) ::
          a("completedAt", classOf[String], Required) ::
          a("status", classOf[String], Required) :: Nil,
        List.empty
      )



    }


    val realWorkflowInstance = existingGraph.Vertex(
      attributes = existingGraph.Attributes(List(
        ExistingAttribute("jobUid", "Super"),
        ExistingAttribute("other1", "One"),
        ExistingAttribute("other3", "TooMuch"),
      )),
      edges = existingGraph.Edges(List.empty)
    )

    println(checkVertex(targetWorkflowInstance, realWorkflowInstance))
  }

  def checkVertexStates(targetVertex: targetGraph.Vertex, existingVertex: existingGraph.Vertex): String = {

  }

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
