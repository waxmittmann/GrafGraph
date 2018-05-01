package templatey

object Main {

  class GraphFactory[T] {

    case class Edge(expectedAttributes: T)

    case class Edges(edges: List[Edge])

    case class Attributes(attributes: Seq[T])

    sealed trait GraphElement

    case class Vertex(
      expectedAttributes: Attributes,
      expectedEdges: Edges
    )

  }

  case class TargetAttribute(name: String, `type`: Class[_], mod: Mod)
  val targetGraph = new GraphFactory[TargetAttribute]

  case class ExistingAttribute(name: String, value: Object)
  val existingGraph = new GraphFactory[ExistingAttribute]


  sealed trait Mod
  case object Required extends Mod
  case object Optional extends Mod


  def main(args: Array[String]): Unit = {

    val targetWorkflowInstance = targetGraph.Vertex(
      expectedAttributes = targetGraph.Attributes(
        List(TargetAttribute("jobUid", classOf[String], Required))
      ),
      expectedEdges = targetGraph.Edges(List.empty)
    )


    val realWorkflowInstance = existingGraph.Vertex(
      expectedAttributes = existingGraph.Attributes(
        List(ExistingAttribute("jobUid", "Super"))
      ),
      expectedEdges = existingGraph.Edges(List.empty)
    )

    println(checkVertex(targetWorkflowInstance, realWorkflowInstance))
  }

  def checkVertex(targetVertex: targetGraph.Vertex, existingVertex: existingGraph.Vertex): String = {

    val target = targetVertex.expectedAttributes.attributes
      .map(ta => (ta.name, ta)).toMap

    val existing = existingVertex.expectedAttributes.attributes
      .map(ta => (ta.name, ta)).toMap


    val targetExisting = (target.keySet ++ existing.keySet).map { key =>
      (target.get(key), existing.get(key))
    }

    val result = targetExisting.map {

      // Todo: Try extractors
      case (Some(target), Some(existing)) => {
//        classOf[ev]

        if (!target.`type`.isInstance(existing.value)) {

          val str = existing.asInstanceOf[String]

    //        if (target.`type` != classOf[ev]) {
//        if (!target.`type`.equals(existing.getClass)) {
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
