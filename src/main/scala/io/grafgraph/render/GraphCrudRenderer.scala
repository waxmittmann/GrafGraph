package io.grafgraph.render
import cats.data.NonEmptyList
import io.grafdefinition.WithBuilders
import io.grafgraph.example.{Attr, Attribute, Lake}
import java.io

import org.scalafmt.config.{NewlineCurlyLambda, ScalafmtConfig}

object GraphCrudRenderer {

  /*
  import java.util.UUID
   */

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

  def renderAttribute(prefix: String)(attr: Attribute): String = {
    s"$prefix${attr.name}: ${attrType(attr)}${attrValue(attr)}"
  }

  private def uncapitalize(str: String) = str.head.toLower + str.tail

  def renderEdge(prefix: String)(selfName: String)(e: WithBuilders[Attribute]#Edge): String = indent(2)({
    //  def writeEdge(self: GraphDefinition[Attribute]#Vertex, e: GraphDefinition[Attribute]#Edge): String = {

    val edgeName = uncapitalize(e.name)

    val r = e match {
      // Hmm ok, so here I need a concrete instance
      case Lake.OtherEdge(_, to: Lake.Vertex, optional: Boolean, toMany: Boolean, attribute: Seq[Attribute]) => {

        // Ignore attribute for now
        // don't do optional; use toMany for that
        if (toMany) {
          s"${edgeName}s: Seq[${to.name}.${to.name}]"
        } else {
          s"$edgeName: ${to.name}.${to.name}"
        }

      }

      case Lake.SelfEdge(_, attribute, optional, toMany) => {
        if (toMany) {
          s"${edgeName}s: Seq[$selfName]"
        } else {
          s"$edgeName: $selfName"
        }

      }
    }

    s"$prefix$r"
  })

  def renderClazz(clazz: WithBuilders[Attribute]#Clazz): String = indent(2)({
    s"""
      |sealed trait ${clazz.name} {
      |${indent(2)(clazz.attributes.map(renderAttribute("val")).mkString(",\n"))}
      |${indent(2)(clazz.edges.map(renderEdge("val")(clazz.name)).mkString(",\n"))}
      |}
    """.stripMargin
  })

  def renderState(
    index: Int, singleState: Boolean,
    vertex: WithBuilders[Attribute]#Vertex,
    state: WithBuilders[Attribute]#VertexState
  ): String = indent(2) {
    val interfaces: Seq[String] = vertex.clazz.map { c =>
      c.name :: vertex.name :: s"New" :: Nil
    }.getOrElse(
      vertex.name :: s"New" :: Nil
    )

    val attrs = state.attributes.map(renderAttribute("")) ++: state.edges.map(renderEdge("")(vertex.name))

    val extendsPart = interfaces match {
      case (ls: List[String]) => s"extends ${ls.mkString(" with ")}"
      case Nil => ""
    }

    val name = state.name.getOrElse(
      if (singleState)
        "Instance"
      else
        s"State_$index"
    )

    s"""
       |case class $name(
       |
       |${indent(2)(attrs.mkString(",\n"))}
       |
       |) $extendsPart
     """.stripMargin
  }


  def renderVersion(vertex: WithBuilders[Attribute]#Vertex, last: WithBuilders[Attribute]#VertexVersion): String =
    s"""
       |
       |  case class ByUid(uid: UUID) extends VertexByUid with ${vertex.name}
       |  case class ByQuery(query: String) extends VertexByQuery with ${vertex.name}
       |  sealed trait New extends VertexNew[${vertex.name}]
       |
       |${indent(2)(last.states.zipWithIndex.map { case (state, index) =>
      renderState(index, last.states.length == 1, vertex, state)
    }.toList.mkString("\n"))}
     """.stripMargin

  def renderVertex(vertex: WithBuilders[Attribute]#Vertex): String = {
    indent(2)({
      s"""
         |object ${vertex.name} {
         |  sealed trait ${vertex.name}

         |${indent(2)(renderVersion(vertex, vertex.versions.head))}
         |
         |  def create(new${uncapitalize(vertex.name)}: New): New = ???
         |
         |}
    """.stripMargin
    })
  }

  // Todo: Make all this configurable
  def render(graph: WithBuilders[Attribute]): String = {
    val packageName = "io.testgraph"
    val imports = Seq[String]("java.util.UUID")

    //${renderMeta(graph.meta)}
    val output = s"""
       |package $packageName
       |
       |${imports.map(i => s"import $i").mkString("\n")}
       |
       |object Graph {
       |  sealed trait CreateReadUpdate
       |  sealed trait VertexByUid extends CreateReadUpdate { val uid: UUID }
       |  sealed trait VertexByQuery extends CreateReadUpdate { val query: String }
       |  sealed trait VertexNew[A] extends CreateReadUpdate
       |
       |   // $${renderMeta(graph.meta)} // Not implemented
       |${indent(2)(graph.allClazzez.map(renderClazz).mkString("\n"))}
       |${indent(2)(graph.allVertices.map(renderVertex).mkString("\n"))}
       |}
     """.stripMargin

    /*
    @DeriveConfDecoder
case class Newlines(
    neverInResultType: Boolean = false,
    neverBeforeJsNative: Boolean = false,
    sometimesBeforeColonInMethodReturnType: Boolean = true,
    penalizeSingleSelectMultiArgList: Boolean = true,
    alwaysBeforeCurlyBraceLambdaParams: Boolean = false,
    alwaysBeforeTopLevelStatements: Boolean = false,
    afterCurlyLambda: NewlineCurlyLambda = NewlineCurlyLambda.never,
    afterImplicitKWInVerticalMultiline: Boolean = false,
    beforeImplicitKWInVerticalMultiline: Boolean = false,
    alwaysBeforeElseAfterCurlyIf: Boolean = false,
    alwaysBeforeMultilineDef: Boolean = true
)

     */
    val newlines = ScalafmtConfig.default.newlines
      .copy(
        neverInResultType = false,
        neverBeforeJsNative = false,
        sometimesBeforeColonInMethodReturnType = true,
        penalizeSingleSelectMultiArgList = true,
        alwaysBeforeCurlyBraceLambdaParams = true,
        alwaysBeforeTopLevelStatements = true,
        afterCurlyLambda = NewlineCurlyLambda.always,
        afterImplicitKWInVerticalMultiline = false,
        beforeImplicitKWInVerticalMultiline = false,
        alwaysBeforeElseAfterCurlyIf = true,
        alwaysBeforeMultilineDef = true
      )
    val config = ScalafmtConfig.default.copy(newlines = newlines)

    org.scalafmt.Scalafmt.format(output, config).get
  }


  def indent(chars: Int)(str: String): String =
    str.linesWithSeparators.map(line => (" " take chars) + line).mkString//.mkString("\n")

}
