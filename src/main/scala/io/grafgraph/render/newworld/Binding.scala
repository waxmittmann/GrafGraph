package io.grafgraph.render.newworld

import io.grafdefinition.WithBuilders
import io.grafgraph.definition.Attribute
import io.grafgraph.example.Lake
import io.grafgraph.example.Lake._

//rootEle = createElement(root)
//edges.map(renderer(rootEle))

sealed trait Binding

case class Value[S](v: S)
case class Node[S](v: S)

case class RenderedOutput[S](
  statement: String,
  bindings: Map[String, Binding] = Map.empty
)

//sealed trait Vertex {
//  val edges: Seq[Edge] = ???
//
//  val attributes: Seq[Attribute] = ???
//
//  def allLabels : String= ???
//}

//sealed trait Attribute {}

//sealed trait Edge {
//  def name: String= ???
//}

object Renderers {
//  val renderers: Seq[Vertex => (Vertex => (Edge => RenderedOutput[String]))] =
  val renderers: Seq[VertexState => VertexState => Edge => String => RenderedOutput[String]] =
    Seq(matchByUidRenderer _, matchByAttributesRenderer _, createRenderer _)


  def main(args: Array[String]): Unit = {
//    println(Lake.workflowInstance.states.map(rootRenderer).map(_.statement).mkString("----\n"))
    println(rootRenderer(Lake.workflowInstance.states.head).statement)
  }


  /* Vertex state 'show's */
  def scriptName(v: VertexState): String = v.name.toLowerCase

  def labels(v: VertexState): String = s":${v.name.toUpperCase} :${v.parent.name.toUpperCase}"

  def renderAttribute(`this`: VertexState)(attr: Attribute): String = s"${attr.name}: ${scriptName(`this`)}.${attr.name}"

  /* Root renderer */
  def rootRenderer(`this`: VertexState): RenderedOutput[String] = {
    val renderedEdges: Seq[RenderedOutput[String]] = `this`.edges.flatMap { e =>
      renderers.map { r =>
        e match {
          case OtherEdge(name, to, optional, toMany, attribute) => r(to)(`this`)(e)("  ")

          case SelfEdge(name, attribute, optional, toMany) => ???
        }
      }
    }

    RenderedOutput(
      s"""
        |CREATE ${renderFullVertexState(`this`, "")}
        |WITH ${scriptName(`this`)}
        ${renderedEdges.map(_.statement).mkString("\n")}
        |RETURN ${scriptName(`this`)}
      """.stripMargin
    )
  }

  /* Renderers */
  def matchByUidRenderer(`this`: VertexState)(parent: VertexState)(edge: Edge)(indent: String): RenderedOutput[String] = {
    RenderedOutput(indentWith(indent,
      s"""
       |UNWIND ${scriptName(parent)}.${scriptName(`this`)}ByUidList AS ${scriptName(`this`)}
       |MATCH (${scriptName(`this`)} ${labels(`this`)} { uid: ${scriptName(`this`)}.uid })
       |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${scriptName(`this`)})
       |WITH ${scriptName(parent)}
      """.stripMargin
    ))
  }

  def matchByAttributesRenderer(`this`: VertexState)(parent: VertexState)(edge: Edge)(indent: String): RenderedOutput[String] = {
    RenderedOutput(indentWith(indent,
      s"""
      |UNWIND ${scriptName(parent)}.${scriptName(`this`)}ByAttributesList AS ${scriptName(`this`)}
      |MATCH (${scriptName(`this`)} ${labels(`this`)} { ${`this`.instanceAttributes.map(renderAttribute(`this`)).mkString(",")} } )
      |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${scriptName(`this`)})
      |WITH ${scriptName(parent)}
      """.stripMargin
    ))
  }

  def createRenderer(`this`: VertexState)(parent: VertexState)(edge: Edge)(indent: String): RenderedOutput[String] = {
  //CREATE (${this.scriptName} :${this.allLabels} { ${this.attributes.map(renderAttribute) } } )

    val renderedEdges: Seq[RenderedOutput[String]] = `this`.edges.flatMap { e =>
      renderers.map { r =>
        e match {
          case OtherEdge(name, to, optional, toMany, attribute) => r(to)(`this`)(e)(indent + "  ")

          case SelfEdge(name, attribute, optional, toMany) => ???
        }
      }
    }

//    RenderedOutput(indentWith(indent,
//      s"""
//      |UNWIND ${scriptName(parent)}.${scriptName(`this`)}ByCreateList AS ${scriptName(`this`)}
//      |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${renderFullVertexState(`this`, "")})
//      |UNWIND ${scriptName(parent)}.${scriptName(`this`)}List AS ${scriptName(`this`)}
//      ${renderedEdges.map(_.statement).mkString("\n")}
//      |WITH ${scriptName(parent)}
//      """.stripMargin
//    ))

    RenderedOutput(indentWith(indent,
      s"""
         |UNWIND ${scriptName(parent)}.${scriptName(`this`)}ByCreateList AS ${scriptName(`this`)}
         |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${renderFullVertexState(`this`, "")})
         |WITH ${scriptName(parent)}, ${scriptName(`this`)}
         ${renderedEdges.map(_.statement).mkString("\n")}
      """.stripMargin
    ))

  }

  /* Helpers */
  def renderFullVertexState(`this`: VertexState, indent: String) =
    s"""$indent(${scriptName(`this`)} ${labels(`this`)} { ${`this`.instanceAttributes.map(renderAttribute(`this`)).mkString(",")} } )"""


  def indentWith(indent: String, text: String): String =
    text.lines.map { line => s"$indent$line"}.mkString("\n")
}
