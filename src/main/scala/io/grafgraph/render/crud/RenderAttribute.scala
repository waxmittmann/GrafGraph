package io.grafgraph.render.crud

import io.grafgraph.definition.{Attr, Attribute}

object RenderAttribute {

  def renderAttribute(prefix: String)(attr: Attribute): String =
    s"$prefix${attr.name}: ${attrType(attr)}${attrValue(attr)}"

  private def attrType(attr: Attribute): String = attr match {
    case Attr.Int(_, _) => "Int"
    case Attr.String(_, _) => "String"
    case Attr.UID(_) => "UUID"
    case Attr.Boolean(_, _) => "Boolean"
  }

  private def attrValue(attr: Attribute): String = attr match {
    case Attr.Int(_, Some(value)) => s"= $value"
    case Attr.String(_, Some(value)) => s"""= "$value""""
    case Attr.Boolean(_, Some(value)) => if (value) "= true" else "= false"
    case _ => ""
  }


}
