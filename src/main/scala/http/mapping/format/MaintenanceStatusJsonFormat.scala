package com.github.scala_training
package http.mapping.format

import domain.enums.MaintenanceStatus

import spray.json.{JsString, JsValue, JsonFormat, deserializationError}

import scala.util.Try

implicit object MaintenanceStatusJsonFormat extends JsonFormat[MaintenanceStatus] {
  def write(x: MaintenanceStatus) = {
    require(x ne null)
    JsString(x.toString)
  }

  def read(value: JsValue) = {
    def stringToMaintenanceStatus(s: String): MaintenanceStatus = Try(MaintenanceStatus.valueOf(s))
      .recover { case t => deserializationError("Expected a valid MaintenanceStatus, but got " + s) }
      .get

    value match {
      case JsString(x) => stringToMaintenanceStatus(x)
      case x => deserializationError("Expected MaintenanceStatus as JsString, but got " + x)
    }
  }
}