package templatey.b

//import templatey.b.Lake.Attr.{Boolean, Int, String, UID}
import templatey.b.Lake._

object GraphLibraryFactory {

  sealed trait Writer {
    def write(str: String): Writer
  }

  case class WriterImpl(cur: Seq[String] = Seq.empty) extends Writer {
    def write(str: String): Writer = WriterImpl(str +: cur)
  }

  def write(writer: Writer)(definitions: Seq[Lake.graph.Vertex]): Writer = {
    // Create trait that everything will belong to, with these as vars (or empty)
    // graph.GlobalAttributes

    val graphElement = s"""sealed trait GraphElement {
       |${graph.GlobalAttributes.map(writeAttributeToTrait).map(s => s"  $s\n").mkString}
       |}
     """.stripMargin


    val intial = writer
      .write("object Graph {")
      .write(graphElement)

    val result = definitions.foldLeft(intial) { case (wi, vert) => wi.write(writeDefinition(vert)) }

    result.write("}")
  }

  def write(definitions: Seq[Lake.graph.Vertex]): WriterImpl = {
   write(WriterImpl())(definitions).asInstanceOf[WriterImpl]
    // definitions.foldLeft(WriterImpl()) { case (wi, vert) => wi.write(writeDefinition(vert)) }
  }

  private def uncapitalize(str: String) = str.head.toLower + str.tail

  def writeEdge(self: graph.Vertex, e: graph.Edge): String = {
    e match {
      case graph.OtherEdge(name: String, to: graph.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Lake.Attribute]) => {

        // Ignore attribute for now
        // don't do optional; use toMany for that
        if (toMany) {
          s"${uncapitalize(name)}s: Seq[${to.name}],"
        } else {
          s"${uncapitalize(name)}: ${to.name},"
        }

      }

      case graph.SelfEdge(name, attribute, optional, toMany) => {
        if (toMany) {
          s"${uncapitalize(name)}s: Seq[${self.name}],"
        } else {
          s"${uncapitalize(name)}: ${self.name},"
        }

      }
    }
  }

  def writeAllowedDefinition(
    vertex: graph.Vertex,
    allowedDefinition: graph.VertexState,
    index: Option[String] = None
  ): String = {
    s"""
       |case class ${vertex.name}${index.fold("")(s => s"_$s")}(
       |${allowedDefinition.attributes.map(writeAttribute).map(s => s"  $s\n").mkString}
       |${allowedDefinition.edges.map(e => writeEdge(vertex, e)).map(s => s"  $s\n").mkString}
      |) ${index.fold("extends GraphElement")(_ => s"extends GraphElement with ${vertex.name}")}
     """.stripMargin
  }

  def attrType(attr: Lake.Attribute): String = attr match {
    case Attr.Int(_, _) => "Int"
    case Attr.String(_, _) => "String"
    case Attr.UID(_, _) => "String"
    case Attr.Boolean(_, _) => "Boolean"
  }

  def attrValue(attr: Lake.Attribute): String = attr match {
    case Attr.Int(_, Some(value)) => s"= $value"
    case Attr.String(_, Some(value)) => s"""= "$value""""
    case Attr.UID(_, Some(value)) => s"""= "$value""""
    case Attr.Boolean(_, Some(value)) => if (value) "= true" else "= false"
    case _ => ""
  }



  def writeAttribute(attr: Lake.Attribute): String = {
    s"${attr.name}: ${attrType(attr)}${attrValue(attr)},"
  }

  def writeAttributeToTrait(attr: Lake.Attribute): String = {
    s"val ${attr.name}: ${attrType(attr)}${attrValue(attr)}"
  }

  def writeDefinition(vertex: Lake.graph.Vertex): String = {

    if (vertex.latest.allowedDefinitions.lengthCompare(1) == 0) {
      val defn = vertex.latest.allowedDefinitions.head
      writeAllowedDefinition(vertex, defn)
    } else {
      s"sealed trait ${vertex.name}" +
        vertex.latest.allowedDefinitions.zipWithIndex.map { case (allowedDefinition: graph.VertexState, index: Int) =>
          writeAllowedDefinition(vertex, allowedDefinition, Some(allowedDefinition.name.getOrElse(index.toString)))
        }.mkString("\n\n")
    }

  }

}
