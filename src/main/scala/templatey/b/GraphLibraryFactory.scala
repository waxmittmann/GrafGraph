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
       | ${graph.GlobalAttributes.map(writeAttributeToTrait).mkString("\n")}
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


  def writeEdge(self: graph.Vertex, e: graph.Edge): String = {
    e match {
      case graph.OtherEdge(to: graph.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Lake.Attribute]) => {

        // Ignore attribute for now
        // don't do optional; use toMany for that

        if (toMany) {
          s"${to.name.head.toLower + to.name.tail}s: Seq[${to.name}],"
        } else {
          s"${to.name.head.toLower + to.name.tail}: ${to.name},"
        }

      }

      case graph.SelfEdge(attribute, optional, toMany) => {
        if (toMany) {
          s"${self.name.head.toLower + self.name.tail}s: Seq[${self.name}],"
        } else {
          s"${self.name.head.toLower + self.name.tail}: ${self.name},"
        }

      }
    }
  }

  def writeAllowedDefinition(
    vertex: graph.Vertex,
    allowedDefinition: graph.VertexDefinition,
    index: Option[Int] = None
  ): String = {
    s"""
       |case class ${vertex.name}(
       |  ${allowedDefinition.attributes.map(writeAttribute).mkString("\n")}
       |  ${allowedDefinition.edges.map(e => writeEdge(vertex, e)).mkString("\n")}
      |) ${index.fold(" extends GraphElement")(s => s"_$s extends GraphElement, ${vertex.name}")}
     """.stripMargin
  }

  def attrType(attr: Lake.Attribute) = attr match {
    case Attr.Int(_, _) => "Int"
    case Attr.String(_, _) => "String"
    case Attr.UID(_, _) => "String"
    case Attr.Boolean(_, _) => "Boolean"
  }

  def attrValue(attr: Lake.Attribute) = attr match {
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
    /*
        case class WorkflowDefn(
          uid: String,
          definition: String,
          artifacts: Seq[Artifact],
        )

     */


    if (vertex.latest.allowedDefinitions.length == 1) {
      val defn = vertex.latest.allowedDefinitions.head
      writeAllowedDefinition(vertex, defn)
    } else {
      s"sealed trait ${vertex.name}" +
        vertex.latest.allowedDefinitions.zipWithIndex.map { case (allowedDefinition: graph.VertexDefinition, index: Int) =>
          writeAllowedDefinition(vertex, allowedDefinition, Some(index))
        }.mkString("\n\n")
    }

  }

}
