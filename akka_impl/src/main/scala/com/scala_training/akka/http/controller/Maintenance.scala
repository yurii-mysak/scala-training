package com.scala_training.akka.http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.scala_training.akka.persistence.model.maintenance.Maintenance
import com.scala_training.akka.persistence.repository.MaintenanceRepository
import com.scala_training.core.domain.adt.{MaintenanceStatus, MaintenanceType}
import com.scala_training.core.http.api.maintenance.get.{GetMaintenanceByIdResponse, GetMaintenancesResponse}
import com.scala_training.core.http.api.maintenance.post.{CreateMaintenanceRequest, CreateMaintenanceResponse}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import java.util.UUID

object Maintenance extends FailFastCirceSupport {

  import io.circe.generic.auto._

  def routes(maintenanceRepository: MaintenanceRepository)(
    implicit ec: ExecutionContext
  ): Route = concat(
    pathPrefix("maintenance") {
      concat(
        pathEnd {
          post {
            entity(as[CreateMaintenanceRequest]) { maintenanceInfo =>
              val id             = UUID.randomUUID()
              val currentDate    = LocalDateTime.now()
              val validatedTypes =
                maintenanceInfo.maintenanceTypes.map(maintenanceType => MaintenanceType.fromUUID(maintenanceType))
              // todo: check car exists

              val maintenance = new Maintenance(
                id,
                maintenanceInfo.carId,
                maintenanceInfo.description,
                validatedTypes,
                Some(maintenanceInfo.scheduledDate),
                MaintenanceStatus.Created,
                currentDate,
                Some(currentDate)
              )

              onComplete(maintenanceRepository.create(maintenance)) {
                case Success(value) => complete(StatusCodes.Created, value: CreateMaintenanceResponse[Maintenance])
                case Failure(ex)    =>
                  complete(
                    StatusCodes.InternalServerError,
                    s"An error occurred while creating a maintenance: ${ex.getMessage}"
                  )
              }
            }
          }
        },
        concat(
          path(JavaUUID) { id =>
            get {
              onComplete(maintenanceRepository.getById(id)) {
                case Success(car) => complete(StatusCodes.OK, car: GetMaintenanceByIdResponse[Maintenance])
                case Failure(ex)  => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
              }
            }
          },
          path(JavaUUID / "schedule") { id =>
            put {
              onComplete(maintenanceRepository.schedule(id)) {
                case Success(maintenance) =>
                  complete(StatusCodes.OK, maintenance: GetMaintenanceByIdResponse[Maintenance])
                case Failure(ex)          =>
                  complete(StatusCodes.InternalServerError, s"Error scheduling maintenance: ${ex.getMessage}")
              }
            }
          }
        )
      )
    },
    // todo: add filtering
    pathPrefix("maintenances") {
      pathEnd {
        get {
          onComplete(maintenanceRepository.getAll) {
            case Success(cars) => complete(StatusCodes.OK, cars: GetMaintenancesResponse[Maintenance])
            case Failure(ex)   => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
          }
        }
      }
    }
  )
}
