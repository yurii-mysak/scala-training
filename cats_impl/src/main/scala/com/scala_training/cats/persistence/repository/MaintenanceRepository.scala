package com.scala_training.cats.persistence.repository

import cats.effect.Sync
import cats.implicits._
import com.scala_training.cats.persistence.model.Maintenance
import com.scala_training.cats.persistence.model.Maintenance._
import com.scala_training.core.domain.adt.MaintenanceStatus
import com.scala_training.core.persistence.command.CommandResponse
import doobie.util.transactor.Transactor
import doobie.implicits._
import com.scala_training.core.domain.adt.MaintenanceStatus._
import doobie.postgres.implicits._
import org.typelevel.log4cats.Logger

import java.util.UUID
import java.time.LocalDateTime

trait MaintenanceRepositoryAPI[F[_]] {
  def create(maintenance: Maintenance): F[CommandResponse[Maintenance]]
  def get(id: UUID): F[CommandResponse[Maintenance]]
  def getAll: F[CommandResponse[Map[UUID, Maintenance]]]
  def schedule(id: UUID, scheduledDate: LocalDateTime): F[CommandResponse[Maintenance]]
}

class MaintenanceRepository[F[_]: Sync: Logger](xa: Transactor[F]) extends MaintenanceRepositoryAPI[F] {

  override def create(maintenance: Maintenance): F[CommandResponse[Maintenance]] =
    Logger[F].info(s"Creating maintenance with ID: ${maintenance.id}") *> {
      val maintenanceTypes = maintenance.maintenanceTypes.map(_.id).toArray
      sql"""
        INSERT INTO maintenances (
          id, car_id, description, maintenance_types, scheduled_date,
          status, created_at, updated_at
        )
        VALUES (
          ${maintenance.id}, ${maintenance.carId}, ${maintenance.description},
          $maintenanceTypes, ${maintenance.scheduledDate},
          ${maintenance.status}, ${maintenance.createdAt}, ${maintenance.updatedAt}
        )
      """.update.run
        .transact(xa)
        .attempt
        .flatTap {
          case Right(_) =>
            Logger[F].info(s"Successfully created maintenance ${maintenance.id}")
          case Left(e)  =>
            Logger[F].error(s"Failed to create maintenance ${maintenance.id}: ${e.getMessage}")
        }
        .map {
          case Right(_) => CommandResponse.Success(Some(maintenance))
          case Left(e)  => CommandResponse.Failure(e.getMessage)
        }
    }

  override def get(id: UUID): F[CommandResponse[Maintenance]] = Logger[F].info(s"Fetching maintenance with ID: $id") *>
    sql"""
      SELECT id, car_id, description, maintenance_types, scheduled_date,
             status, created_at, updated_at
      FROM maintenances
      WHERE id = $id
    """
      .query[Maintenance]
      .option
      .transact(xa)
      .attempt
      .flatTap {
        case Right(Some(_)) => Logger[F].info(s"Successfully retrieved maintenance $id")
        case Right(None)    => Logger[F].warn(s"Maintenance $id not found")
        case Left(e)        => Logger[F].error(s"Error retrieving maintenance $id: ${e.getMessage}")
      }
      .map {
        case Right(Some(maintenance)) => CommandResponse.Success(Some(maintenance))
        case Right(None)              => CommandResponse.Failure("Maintenance not found")
        case Left(e)                  => CommandResponse.Failure(e.getMessage)
      }

  override def getAll: F[CommandResponse[Map[UUID, Maintenance]]] = sql"""
      SELECT id, car_id, description, maintenance_types, scheduled_date,
             status, created_at, updated_at
      FROM maintenances
    """
    .query[Maintenance]
    .stream
    .compile
    .toList
    .transact(xa)
    .attempt
    .flatTap {
      case Right(maintenances) => Logger[F].info(s"Successfully retrieved ${maintenances.size} maintenances")
      case Left(e)             => Logger[F].error(s"Failed to retrieve maintenances: ${e.getMessage}")
    }
    .map {
      case Right(maintenances) => CommandResponse.Success(Some(maintenances.map(m => m.id -> m).toMap))
      case Left(e)             => CommandResponse.Failure(e.getMessage)
    }

  override def schedule(id: UUID, scheduledDate: LocalDateTime): F[CommandResponse[Maintenance]] =
    Logger[F].info(s"Scheduling maintenance $id for $scheduledDate") *> {
      val now                                = LocalDateTime.now()
      val scheduledStatus: MaintenanceStatus = MaintenanceStatus.Scheduled
      sql"""
        UPDATE maintenances
        SET status = $scheduledStatus, scheduled_date = $scheduledDate, updated_at = $now
        WHERE id = $id
        RETURNING id, car_id, description, maintenance_types, scheduled_date,
                  status, created_at, updated_at
      """
        .query[Maintenance]
        .option
        .transact(xa)
        .attempt
        .flatTap {
          case Right(Some(_)) => Logger[F].info(s"Successfully scheduled maintenance $id")
          case Right(None)    => Logger[F].warn(s"Maintenance $id not found for scheduling")
          case Left(e)        => Logger[F].error(s"Error scheduling maintenance $id: ${e.getMessage}")
        }
        .map {
          case Right(Some(maintenance)) => CommandResponse.Success(Some(maintenance))
          case Right(None)              => CommandResponse.Failure("Maintenance not found")
          case Left(e)                  => CommandResponse.Failure(e.getMessage)
        }
    }
}
