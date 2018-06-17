package io.grafgraph.render

//import io.grafgraph.example.Lake.graph
import io.grafgraph.definition.{Attr, Attribute, GraphDefinition}
import io.grafgraph.example.Simple

// Couldn't use type projection because I want to match on some case classes, and it won't let me based on the type
// projection, nor on a `type X = Y#Z` style thing.
object GraphDaoRenderer {

  sealed trait Writer {
    def write(str: String): Writer
  }

  case class WriterImpl(cur: Seq[String] = Seq.empty) extends Writer {
    def write(str: String): Writer = WriterImpl(str +: cur)
  }

  def write(writer: Writer)(definitions: Seq[Simple.Vertex]): Writer = {
    // Create trait that everything will belong to, with these as vars (or empty)
    // graph.GlobalAttributes

    val graphElement = s"""sealed trait GraphElement {
       |${Simple.GlobalAttributes.map(writeAttributeToTrait).map(s => s"  $s\n").mkString}
       |}
     """.stripMargin


    val intial = writer
      .write("object Graph {")
      .write(graphElement)

    val result = definitions.foldLeft(intial) { case (wi, vert) => wi.write(writeDefinition(vert)) }

    result.write("}")
  }

  def write(definitions: Seq[Simple.Vertex]): WriterImpl = write(WriterImpl())(definitions).asInstanceOf[WriterImpl]

  private def uncapitalize(str: String) = str.head.toLower + str.tail

  def writeEdge(self: Simple.Vertex, e: Simple.Edge): String = {
    // Ignore attribute for now
    // don't do optional; use toMany for that
    if (e.toMany) {
      s"${uncapitalize(e.name)}s: Seq[${e.to.name}],"
    } else {
      s"${uncapitalize(e.name)}: ${e.to.name},"
    }
  }

  def writeAllowedDefinition(
    vertex: Simple.Vertex,
    allowedDefinition: Simple.VertexState,
    index: Option[String] = None
  ): String = {
    s"""
       |case class ${vertex.name}${index.fold("")(s => s"_$s")}(
       |${allowedDefinition.allAttributes.map(writeAttribute).map(s => s"  $s\n").mkString}
       |${allowedDefinition.edges.map(e => writeEdge(vertex, e)).map(s => s"  $s\n").mkString}
      |) ${index.fold("extends GraphElement")(_ => s"extends GraphElement with ${vertex.name}")}
     """.stripMargin
  }

  def attrType(attr: Attribute): String = attr match {
    case Attr.Int(_, _) => "Int"
    case Attr.String(_, _) => "String"
    case Attr.UID(_) => "UUID"
    case Attr.Boolean(_, _) => "Boolean"
  }

  def attrValue(attr: Attribute): String = attr match {
    case Attr.Int(_, Some(value)) => s"= $value"
    case Attr.String(_, Some(value)) => s"""= "$value""""
    case Attr.Boolean(_, Some(value)) => if (value) "= true" else "= false"
    case _ => ""
  }

  def writeAttribute(attr: Attribute): String = {
    s"${attr.name}: ${attrType(attr)}${attrValue(attr)},"
  }

  def writeAttributeToTrait(attr: Attribute): String = {
    s"val ${attr.name}: ${attrType(attr)}${attrValue(attr)}"
  }

  def writeDefinition(vertex: Simple.Vertex): String = {

    if (vertex.states.length == 1) {
      val defn = vertex.states.head
      writeAllowedDefinition(vertex, defn)
    } else {
      s"sealed trait ${vertex.name}" +
        vertex.states.zipWithIndex.map { case (allowedDefinition: Simple.VertexState, index: Int) =>
          writeAllowedDefinition(vertex, allowedDefinition, Some(allowedDefinition.name))
        }.toList.mkString("\n\n")
    }
  }
}
