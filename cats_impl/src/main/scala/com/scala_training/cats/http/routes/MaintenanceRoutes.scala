package com.scala_training.cats.http.routes

import cats.effect.Concurrent
import cats.implicits.*
import com.scala_training.cats.application.MaintenanceService
import com.scala_training.cats.http.handlers
import com.scala_training.cats.persistence.model.Maintenance
import com.scala_training.core.domain.adt.{MaintenanceStatus, MaintenanceType}
import com.scala_training.core.http.api.maintenance.post.CreateMaintenanceRequest
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import java.time.LocalDateTime
import java.util.UUID

class MaintenanceRoutes[F[_]: {Concurrent, Logger}](
  maintenanceService: MaintenanceService[F],
  errorHandler: handlers.HttpErrorHandler[F],
  responseHandler: handlers.MaintenanceResponseHandler[F]
) extends Http4sDsl[F] {
  import io.circe.generic.auto.*

  given createMaintenanceReqDecoder: EntityDecoder[F, CreateMaintenanceRequest] = jsonOf[F, CreateMaintenanceRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "api" / "maintenance" =>
      (for {
        _      <- Logger[F].info("Attempting to create new maintenance")
        mReq   <- req.as[CreateMaintenanceRequest]
        types  <- validateMaintenanceTypes(mReq.maintenanceTypes)
        result <- maintenanceService.createMaintenance(mReq, types).attempt
        resp   <- responseHandler.handleCreateResponse(result)
      } yield resp).handleErrorWith(errorHandler.handleError("Unexpected error during maintenance creation"))

    case GET -> Root / "api" / "maintenance" / UUIDVar(id) =>
      (for {
        _      <- Logger[F].info(s"Fetching maintenance with id: $id")
        result <- maintenanceService.getMaintenance(id).attempt
        resp   <- responseHandler.handleGetResponse(result)
      } yield resp).handleErrorWith(errorHandler.handleError("Unexpected error during maintenance fetch"))

    case PUT -> Root / "api" / "maintenance" / UUIDVar(id) / "schedule" =>
      (for {
        _      <- Logger[F].info(s"Scheduling maintenance with id: $id")
        result <- maintenanceService.scheduleMaintenance(id, LocalDateTime.now()).attempt
        resp   <- responseHandler.handleScheduleResponse(result)
      } yield resp).handleErrorWith(errorHandler.handleError("Unexpected error during maintenance scheduling"))

    case GET -> Root / "api" / "maintenances" =>
      (for {
        _      <- Logger[F].info("Fetching all maintenances")
        result <- maintenanceService.getAllMaintenances.attempt
        resp   <- responseHandler.handleGetAllResponse(result)
      } yield resp).handleErrorWith(errorHandler.handleError("Unexpected error during maintenances fetch"))
  }

  // todo: what it does?
  private def validateMaintenanceTypes(types: List[UUID]): F[List[MaintenanceType]] =
    types.traverse(uuid => Concurrent[F].pure(MaintenanceType.fromUUID(uuid)))
}
