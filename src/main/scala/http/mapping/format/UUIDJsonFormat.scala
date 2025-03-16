package com.github.scala_training
package http.mapping.format

import spray.json.{JsString, JsValue, JsonFormat, deserializationError}

import java.util.UUID
import scala.util.Try

implicit object UUIDJsonFormat extends JsonFormat[UUID] {
  def write(x: UUID) = {
    require(x ne null)
    JsString(x.toString)
  }

  def read(value: JsValue) = {
    def stringToUUID(s: String): UUID = Try(UUID.fromString(s))
      .recover { case t => deserializationError("Expected a valid UUID, but got " + s) }
      .get

    value match {
      case JsString(x) => stringToUUID(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
}