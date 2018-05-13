package io.grafgraph.render.crud

import io.grafdefinition.WithBuilders
import io.grafgraph.render.crud.Util.indent
import org.scalafmt.config.{NewlineCurlyLambda, ScalafmtConfig}

object GraphCrudRenderer {

  // Todo: Make all this configurable
  def render(graph: WithBuilders): String = {
    val packageName = "io.testgraph"
    val imports = Seq[String]("java.util.UUID", "org.neo4j.driver.v1._", "scala.collection.JavaConverters._")

    val neo4jGraph =
      """
        |
        |class Neo4jGraph(url: String, user: String, password: String) {
        |
        |  private val token: AuthToken = AuthTokens.basic(user, password)
        |
        |  val driver: Driver = GraphDatabase.driver(
        |    url,
        |    token,
        |    Config.build.withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig
        |  )
        |
        |  def close(): Unit = {
        |    driver.close()
        |  }
        |}
        |
      """.stripMargin


    //${renderMeta(graph.meta)}
    val output = s"""
                    |package $packageName
                    |
                   |${imports.map(i => s"import $i").mkString("\n")}
                    |
       |$neo4jGraph
                    |
       |class Graph(graph: Neo4jGraph) {
                    |
       |  sealed trait CreateReadUpdate
                    |  sealed trait VertexByUid extends CreateReadUpdate { val uid: UUID }
                    |  sealed trait VertexByQuery extends CreateReadUpdate { val query: String }
                    |  sealed trait VertexNew[A] extends CreateReadUpdate
                    |
       |   // $${renderMeta(graph.meta)} // Not implemented
                    |${indent(2)(graph.allClazzez.map(RenderClazz.renderClazz).mkString("\n"))}
                    |${indent(2)(graph.allVertices.map(RenderVertex.renderVertex).mkString("\n"))}
                    |}
     """.stripMargin

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

    println(s"PreRender: ${output.linesWithSeparators.zipWithIndex.map { case (line, index) => s"$index: $line"}.mkString}")

    org.scalafmt.Scalafmt.format(output, config).get
  }


}
