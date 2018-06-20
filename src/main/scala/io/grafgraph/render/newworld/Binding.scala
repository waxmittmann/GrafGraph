package io.grafgraph.render.newworld

import io.grafgraph.definition.{Attribute, GraphDefinition}

import cats.instances.all._
import cats.{Applicative, Monoid}
import cats.implicits._
import cats.syntax.all._

sealed trait Binding

case class Value[S](v: S)
case class Node[S](v: S)

case class RenderedOutput(
  statement: List[String],
  bindings: Map[String, Binding]
)

object RenderedOutput {
  def apply(statement: String, bindings: Map[String, Binding] = Map.empty): RenderedOutput = RenderedOutput(List(statement), bindings)
}

object Renderers {
  type VertexState = GraphDefinition#VertexState
  type Vertex = GraphDefinition#Vertex
  type Edge = GraphDefinition#Edge

  implicit val rom: Monoid[RenderedOutput] = new Monoid[RenderedOutput] {
    override def empty: RenderedOutput = RenderedOutput(List.empty[String], Map.empty[String, Binding])

    override def combine(x: RenderedOutput, y: RenderedOutput): RenderedOutput = {
      RenderedOutput(x.statement ++ y.statement, x.bindings ++ y.bindings)
    }
  }

  val renderers: Seq[VertexState => Seq[VertexState] => Edge => String => RenderedOutput] =
    Seq(matchByUidRenderer _, matchByAttributesRenderer _, createRenderer _)


  def main(args: Array[String]): Unit = {
//    println(rootRenderer(io.grafgraph.example.Lake.workflowInstance.states.head).statement.mkString("\n"))
    println(rootRenderer(io.grafgraph.example.Simple.aDefn.states.head).statement.mkString("\n"))
  }

  val printLp: Boolean = false
  def lp(str: String): String = {
    if (printLp)
      s"$str->"//.padTo(100, ' ')
    else
      ""
  }

  def scriptName(v: VertexState): String = s"${v.parent.name}_${v.name.toLowerCase}"
  def dataName(v: VertexState): String = s"${scriptName(v)}Data"
  def labels(v: VertexState): String = s":${v.name.toUpperCase} :${v.parent.name.toUpperCase}"
  def renderAttribute(`this`: VertexState)(attr: Attribute): String = s"${attr.name}: ${dataName(`this`)}.${attr.name}"
  def getParent(ancestors: Seq[VertexState]): VertexState = ancestors.last


  def buildInput(bindings: Map[String, Binding]): String = {

  }

  def render(`this`: VertexState): String = {
    val prefix = lp("createRenderer")

    val header = s"def create(input: ${`this`.parent}.${`this`.name}) {"

    val r = rootRenderer(`this`)
    val inputPart = s"val params = Map[String, AnyRef](${buildInput(r.bindings)}).asJava"
    val queryPart = r.statement.mkString("\n")

    val footer = "}"

    s"$header\n${indentWith("  ", s"$inputPart\n$queryPart")}\n$footer"


  }

  /* Root renderer */
  def rootRenderer(`this`: VertexState): RenderedOutput = {
    val renderedEdges: Seq[RenderedOutput] = `this`.edges.flatMap { e =>
      renderers.map { r => r(e.to)(Seq(`this`))(e)("  ") }
    }

    val prefix = lp("rootRenderer")
    rom.combineAll(
      Seq(RenderedOutput(
        s"""
           |${prefix}WITH $$root AS ${dataName(`this`)}
           |${prefix}CREATE ${renderFullVertexState(`this`, "")}
           |${prefix}WITH ${scriptName(`this`)}
       """.stripMargin
      )) ++
      renderedEdges ++
      Seq(RenderedOutput(
        s"""
          |${prefix}RETURN ${scriptName(`this`)}
          """.stripMargin
      ))
    )
  }

  /* Renderers */
  def matchByUidRenderer(`this`: VertexState)(ancestors: Seq[VertexState])(edge: Edge)(indent: String): RenderedOutput = {
    val prefix = lp("matchByUidRenderer")
    val parent = getParent(ancestors)
    RenderedOutput(indentWith(s"$prefix$indent",
      s"""
       |UNWIND ${dataName(parent)}.${scriptName(`this`)}ByUidList AS ${scriptName(`this`)}
       |MATCH (${scriptName(`this`)} ${labels(`this`)} { uid: ${dataName(`this`)}.uid })
       |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${scriptName(`this`)})
       |WITH ${s"${ancestors.map(scriptName).mkString(",")}"}
      """.stripMargin
    ))
  }

  def matchByAttributesRenderer(`this`: VertexState)(ancestors: Seq[VertexState])(edge: Edge)(indent: String): RenderedOutput = {
    val parent = getParent(ancestors)
    val prefix = lp("matchByAttributesRenderer")

    RenderedOutput(indentWith(s"$prefix$indent",
      s"""
      |UNWIND ${dataName(parent)}.${dataName(`this`)}ByAttributesList AS ${dataName(`this`)}
      |MATCH (${scriptName(`this`)} ${labels(`this`)} { ${`this`.instanceAttributes.map(renderAttribute(`this`)).mkString(",")} } )
      |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${scriptName(`this`)})
      |WITH ${s"${ancestors.map(scriptName).mkString(",")}"}
      """.stripMargin
    ))
  }

  def createRenderer(`this`: VertexState)(ancestors: Seq[VertexState])(edge: Edge)(indent: String): RenderedOutput = {
    val parent = getParent(ancestors)
    val prefix = lp("createRenderer")

    val renderedEdges: Seq[RenderedOutput] = `this`.edges.flatMap { e =>
      renderers.map { r => r(e.to)(`this` +: ancestors)(e)(indent + "  ") }
    }

    rom.combineAll(
      Seq(RenderedOutput(indentWith(s"$prefix$indent",
        s"""
           |UNWIND ${dataName(parent)}.${dataName(`this`)}ByCreateList AS ${dataName(`this`)}
           |CREATE (${scriptName(parent)}) -[:${edge.name}]-> ${renderFullVertexState(`this`, "")}
           |WITH ${s"${ancestors.map(scriptName).mkString(",")}, ${scriptName(`this`)}"}
        """.stripMargin
      ))) ++ renderedEdges
    )
  }

  /* Helpers */
  def renderFullVertexState(`this`: VertexState, indent: String): String = {
    val queryPart = s"""$indent(${scriptName(`this`)} ${labels(`this`)} { ${`this`.instanceAttributes.map(renderAttribute(`this`)).mkString(",")} } )"""

    queryPart
  }


  def indentWith(indent: String, text: String): String =
    text.lines.map { line => s"$indent$line"}.mkString("\n")
}
