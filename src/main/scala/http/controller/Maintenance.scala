package com.github.scala_training
package http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import spray.json.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import http.mapping.MaintenanceJsonProtocol.given

import domain.enums.MaintenanceStatus
import domain.model.api.post.maintenance.{CreateMaintenanceRequest, CreateMaintenanceResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import java.util.UUID

object Maintenance {
  def routes(using ec: ExecutionContext): Route = {
    path("maintenance") {
      post {
        entity(as[CreateMaintenanceRequest]) { request =>
          val responseFuture = Future {
            val id = UUID.randomUUID()
            CreateMaintenanceResponse(id, MaintenanceStatus.Created)
          }

          onComplete(responseFuture) {
            case Success(response) => complete(response)
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
  }
}