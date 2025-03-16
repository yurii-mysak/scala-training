package com.github.scala_training
package http.mapping

import domain.model.api.post.maintenance.{CreateMaintenanceRequest, CreateMaintenanceResponse}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.scala_training.http.mapping.format.UUIDJsonFormat
import com.github.scala_training.http.mapping.format.MaintenanceTypeJsonFormat
import com.github.scala_training.http.mapping.format.MaintenanceStatusJsonFormat
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object MaintenanceJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val maintenanceRequestFormat: RootJsonFormat[CreateMaintenanceRequest] = jsonFormat4(CreateMaintenanceRequest.apply)
  implicit val maintenanceResponseFormat: RootJsonFormat[CreateMaintenanceResponse] = jsonFormat2(CreateMaintenanceResponse.apply)
}
