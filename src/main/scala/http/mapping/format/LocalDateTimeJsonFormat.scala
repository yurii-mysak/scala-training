package com.github.scala_training
package http.mapping.format

import spray.json.{JsString, JsValue, JsonFormat, deserializationError}

import java.time.LocalDateTime
import scala.util.Try

implicit object LocalDateTimeJsonFormat extends JsonFormat[LocalDateTime] {
  def write(x: LocalDateTime) = {
    require(x ne null)
    JsString(x.toString)
  }

  def read(value: JsValue) = {
    def stringToLocalDateTime(s: String): LocalDateTime = Try(LocalDateTime.parse(s))
      .recover { case t => deserializationError("Expected a valid LocalDateTime, but got " + s) }
      .get

    value match {
      case JsString(x) => stringToLocalDateTime(x)
      case x => deserializationError("Expected LocalDateTime as JsString, but got " + x)
    }
  }
}