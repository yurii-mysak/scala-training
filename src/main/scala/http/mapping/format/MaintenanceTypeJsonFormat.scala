package com.github.scala_training
package http.mapping.format

import spray.json.{JsString, JsValue, JsonFormat, deserializationError}
import com.github.scala_training.domain.enums.MaintenanceType

import scala.util.Try

implicit object MaintenanceTypeJsonFormat extends JsonFormat[MaintenanceType] {
  def write(x: MaintenanceType) = {
    require(x ne null)
    JsString(x.toString)
  }

  def read(value: JsValue) = {
    def stringToMaintenanceType(s: String): MaintenanceType = Try(MaintenanceType.valueOf(s))
      .recover { case t => deserializationError("Expected a valid MaintenanceType, but got " + s) }
      .get

    value match {
      case JsString(x) => stringToMaintenanceType(x)
      case x => deserializationError("Expected MaintenanceType as JsString, but got " + x)
    }
  }
}