package io.grafgraph.render

//import io.grafgraph.example.Lake.graph
import io.grafgraph.definition.{Attr, Attribute, GraphDefinition}
import io.grafgraph.example.Lake

// Couldn't use type projection because I want to match on some case classes, and it won't let me based on the type
// projection, nor on a `type X = Y#Z` style thing.
object GraphDaoRenderer {

  sealed trait Writer {
    def write(str: String): Writer
  }

  case class WriterImpl(cur: Seq[String] = Seq.empty) extends Writer {
    def write(str: String): Writer = WriterImpl(str +: cur)
  }

  def write(writer: Writer)(definitions: Seq[Lake.Vertex]): Writer = {
    // Create trait that everything will belong to, with these as vars (or empty)
    // graph.GlobalAttributes

    val graphElement = s"""sealed trait GraphElement {
       |${Lake.GlobalAttributes.map(writeAttributeToTrait).map(s => s"  $s\n").mkString}
       |}
     """.stripMargin


    val intial = writer
      .write("object Graph {")
      .write(graphElement)

    val result = definitions.foldLeft(intial) { case (wi, vert) => wi.write(writeDefinition(vert)) }

    result.write("}")
  }

  def write(definitions: Seq[Lake.Vertex]): WriterImpl = write(WriterImpl())(definitions).asInstanceOf[WriterImpl]

  private def uncapitalize(str: String) = str.head.toLower + str.tail

  def writeEdge(self: Lake.Vertex, e: Lake.Edge): String = {
    e match {
        // Hmm ok, so here I need a concrete instance
      case Lake.OtherEdge(name: String, to: Lake.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Attribute]) => {

        // Ignore attribute for now
        // don't do optional; use toMany for that
        if (toMany) {
          s"${uncapitalize(name)}s: Seq[${to.name}],"
        } else {
          s"${uncapitalize(name)}: ${to.name},"
        }

      }

      case Lake.SelfEdge(name, attribute, optional, toMany) => {
        if (toMany) {
          s"${uncapitalize(name)}s: Seq[${self.name}],"
        } else {
          s"${uncapitalize(name)}: ${self.name},"
        }

      }
    }
  }

  def writeAllowedDefinition(
    vertex: Lake.Vertex,
    allowedDefinition: Lake.VertexState,
    index: Option[String] = None
  ): String = {
    s"""
       |case class ${vertex.name}${index.fold("")(s => s"_$s")}(
       |${allowedDefinition.attributes.map(writeAttribute).map(s => s"  $s\n").mkString}
       |${allowedDefinition.edges.map(e => writeEdge(vertex, e)).map(s => s"  $s\n").mkString}
      |) ${index.fold("extends GraphElement")(_ => s"extends GraphElement with ${vertex.name}")}
     """.stripMargin
  }

  def attrType(attr: Attribute): String = attr match {
    case Attr.Int(_, _) => "Int"
    case Attr.String(_, _) => "String"
    case Attr.UID(_, _) => "String"
    case Attr.Boolean(_, _) => "Boolean"
  }

  def attrValue(attr: Attribute): String = attr match {
    case Attr.Int(_, Some(value)) => s"= $value"
    case Attr.String(_, Some(value)) => s"""= "$value""""
    case Attr.UID(_, Some(value)) => s"""= "$value""""
    case Attr.Boolean(_, Some(value)) => if (value) "= true" else "= false"
    case _ => ""
  }



  def writeAttribute(attr: Attribute): String = {
    s"${attr.name}: ${attrType(attr)}${attrValue(attr)},"
  }

  def writeAttributeToTrait(attr: Attribute): String = {
    s"val ${attr.name}: ${attrType(attr)}${attrValue(attr)}"
  }

  def writeDefinition(vertex: Lake.Vertex): String = {

    if (vertex.latest.states.length == 1) {
      val defn = vertex.latest.states.head
      writeAllowedDefinition(vertex, defn)
    } else {
      s"sealed trait ${vertex.name}" +
        vertex.latest.states.zipWithIndex.map { case (allowedDefinition: Lake.VertexState, index: Int) =>
          writeAllowedDefinition(vertex, allowedDefinition, Some(allowedDefinition.name.getOrElse(index.toString)))
        }.toList.mkString("\n\n")
    }

  }

}
