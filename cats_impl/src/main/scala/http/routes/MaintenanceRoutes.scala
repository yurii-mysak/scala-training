package http.routes

import cats.effect.Sync
import cats.implicits._
import domain.adt.{MaintenanceStatus, MaintenanceType}
import http.api.maintenance.post.CreateMaintenanceRequest
import http.handlers.{HttpErrorHandler, MaintenanceResponseHandler}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import application.MaintenanceService
import persistence.model.Maintenance

import java.time.LocalDateTime
import java.util.UUID

class MaintenanceRoutes[F[_]: Sync: Logger](
  maintenanceService: MaintenanceService[F],
  errorHandler: HttpErrorHandler[F],
  responseHandler: MaintenanceResponseHandler[F]
) extends Http4sDsl[F] {
  import io.circe.generic.auto._

  implicit def createMaintenanceReqDecoder: EntityDecoder[F, CreateMaintenanceRequest] =
    jsonOf[F, CreateMaintenanceRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "api" / "maintenance" =>
      (for {
        _      <- Logger[F].info("Attempting to create new maintenance")
        mReq   <- req.as[CreateMaintenanceRequest]
        types  <- validateMaintenanceTypes(mReq.maintenanceTypes)
        maint   = createMaintenance(mReq, types)
        result <- maintenanceService.createMaintenance(maint).attempt
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
    types.traverse(uuid => Sync[F].pure(MaintenanceType.fromUUID(uuid)))

  private def createMaintenance(req: CreateMaintenanceRequest, types: List[MaintenanceType]): Maintenance = {
    val now = LocalDateTime.now()
    Maintenance(
      UUID.randomUUID(),
      req.carId,
      req.description,
      types,
      Some(req.scheduledDate),
      MaintenanceStatus.Created,
      now,
      Some(now)
    )
  }
}
