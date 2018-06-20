package io.grafgraph.render.newworld

import io.grafgraph.example.Simple

object BindingExample {


  case class A(aVal: String, bs: Seq[B])
  case class B(bVal: String, cs: Seq[C])
  case class C(cVal: String)



  def write(a: A): Unit = {

    s"""
       |WITH $$root AS a
       |CREATE
     """.stripMargin

  }

  def main(args: Array[String]): Unit = {



  }

}
