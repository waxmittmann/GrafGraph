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
  val renderers: Seq[VertexState => VertexState => Edge => RenderedOutput[String]] =
    Seq(matchByUidRenderer _, matchByAttributesRenderer _, createRenderer _)


  def main(args: Array[String]): Unit = {
    println(Lake.workflowInstance.states.map(rootRenderer).mkString("----\n"))
  }


  /* Vertex state 'show's */
  def scriptName(v: VertexState): String = v.name.toLowerCase

  def labels(v: VertexState): String = s":${v.name.toUpperCase} :${v.parent.name.toUpperCase}"

  def renderAttribute(attr: Attribute): String = s"${attr.name}: ${attr.name}"

  /* Root renderer */
  def rootRenderer(`this`: VertexState): RenderedOutput[String] = {
    RenderedOutput(
      s"""
        |CREATE ${renderFullVertexState(`this`)}
        ${ `this`.edges.map { e =>
          renderers.map { r =>
            e match {
              case OtherEdge(name, to, optional, toMany, attribute) => r(to)(`this`)(e)

              case SelfEdge(name, attribute, optional, toMany) => ???
            }
          }
        }}
        |RETURN ${scriptName(`this`)}
      """
    )
  }

  /* Renderers */
  def matchByUidRenderer(`this`: VertexState)(parent: VertexState)(edge: Edge): RenderedOutput[String] = {
    RenderedOutput(
      s"""
       |MATCH (${scriptName(`this`)} :${labels(`this`)} { uid: $$${scriptName(`this`)}Uid })
       |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${scriptName(`this`)})
       |RETURN ${scriptName(parent)}
      """
    )
  }

  def matchByAttributesRenderer(`this`: VertexState)(parent: VertexState)(edge: Edge): RenderedOutput[String] = {
    RenderedOutput(
      s"""
      |MATCH (${scriptName(`this`)} :${labels(`this`)} { ${`this`.instanceAttributes.map(renderAttribute).mkString(",")} } )
      |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${scriptName(`this`)})
      |RETURN ${scriptName(parent)}
      """
    )
  }

  def createRenderer(`this`: VertexState)(parent: VertexState)(edge: Edge): RenderedOutput[String] = {
  //CREATE (${this.scriptName} :${this.allLabels} { ${this.attributes.map(renderAttribute) } } )
    RenderedOutput(
      s"""
      |CREATE (${scriptName(parent)}) -[:${edge.name}]-> (${renderFullVertexState(`this`)})
      |UNWIND ${scriptName(parent)}.${scriptName(`this`)}List AS ${scriptName(`this`)}
      ${ `this`.edges.map { e =>
        renderers.map { r =>
          e match {
            case OtherEdge(name, to, optional, toMany, attribute) => r(to)(`this`)(e)

            case SelfEdge(name, attribute, optional, toMany) => ???
          }
        }
      }}
      |RETURN ${scriptName(parent)}
      """
    )
  }

  /* Helpers */
  def renderFullVertexState (`this`: VertexState) =
    s"""(${scriptName(`this`)} :${labels(`this`)} { ${`this`.instanceAttributes.map(renderAttribute).mkString(",")} } )"""

}
