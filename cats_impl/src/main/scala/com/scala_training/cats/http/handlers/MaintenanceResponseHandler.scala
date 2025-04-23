package com.scala_training.cats.http.handlers

import cats.effect.{Concurrent, Sync}
import cats.implicits.*
import com.scala_training.core.domain.model.MaintenanceCore
import com.scala_training.core.http.api.maintenance.get.{GetMaintenanceByIdResponse, GetMaintenancesResponse}
import com.scala_training.core.http.api.maintenance.post.CreateMaintenanceResponse
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, Response}
import org.typelevel.log4cats.Logger
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID

class MaintenanceResponseHandler[F[_]: {Concurrent, Logger}] extends Http4sDsl[F] {
  import io.circe.generic.auto.*

  given createMaintenanceRespEncoder[A <: MaintenanceCore]: EntityEncoder[F, CreateMaintenanceResponse[A]] =
    jsonEncoderOf[CreateMaintenanceResponse[A]]

  given getMaintenanceByIdRespEncoder[A <: MaintenanceCore]: EntityEncoder[F, GetMaintenanceByIdResponse[A]] =
    jsonEncoderOf[GetMaintenanceByIdResponse[A]]

  given getMaintenancesRespEncoder[A <: MaintenanceCore]: EntityEncoder[F, GetMaintenancesResponse[A]] =
    jsonEncoderOf[GetMaintenancesResponse[A]]

  def handleCreateResponse[A <: MaintenanceCore](result: Either[Throwable, CommandResponse[A]]): F[Response[F]] =
    result match {
      case Right(response) =>
        response match {
          case CommandResponse.Success(Some(maintenance)) =>
            Logger[F].info(s"Successfully created maintenance with id: ${maintenance.id}") *>
              Created(response: CreateMaintenanceResponse[A])
          case CommandResponse.Success(None)              =>
            Conflict("Maintenance creation failed")
          case CommandResponse.Failure(reason)            =>
            Conflict(reason)
        }
      case Left(err)       =>
        Logger[F].error(s"Failed to create maintenance: ${err.getMessage}") *> InternalServerError()
    }

  def handleGetResponse[A <: MaintenanceCore](result: Either[Throwable, CommandResponse[A]]): F[Response[F]] =
    result match {
      case Right(maintenanceByIdCommandResponse @ CommandResponse.Success(Some(_))) =>
        Ok(maintenanceByIdCommandResponse: GetMaintenanceByIdResponse[A])
      case Right(CommandResponse.Success(None))                                     =>
        NotFound("Maintenance not found")
      case Right(CommandResponse.Failure(reason))                                   =>
        NotFound(reason)
      case Left(err)                                                                =>
        Logger[F].error(s"Error fetching maintenance: ${err.getMessage}") *> InternalServerError()
    }

  def handleGetAllResponse[A <: MaintenanceCore](
    result: Either[Throwable, CommandResponse[Map[UUID, A]]]
  ): F[Response[F]] = result match {
    case Right(response) => Ok(response: GetMaintenancesResponse[A])
    case Left(err)       =>
      Logger[F].error(s"Failed to fetch maintenances: ${err.getMessage}") *> InternalServerError()
  }

  def handleScheduleResponse[A <: MaintenanceCore](result: Either[Throwable, CommandResponse[A]]): F[Response[F]] =
    result match {
      case Right(maintenanceScheduledCommandResponse @ CommandResponse.Success(Some(maintenance))) =>
        Logger[F].info(s"Successfully scheduled maintenance with id: ${maintenance.id}") *>
          Ok(maintenanceScheduledCommandResponse: GetMaintenanceByIdResponse[A])
      case Right(CommandResponse.Success(None))                                                    =>
        NotFound("Maintenance not found")
      case Right(CommandResponse.Failure(reason))                                                  =>
        NotFound(reason)
      case Left(err)                                                                               =>
        Logger[F].error(s"Error scheduling maintenance: ${err.getMessage}") *> InternalServerError()
    }
}
