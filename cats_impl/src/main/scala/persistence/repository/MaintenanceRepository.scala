package persistence.repository

import cats.effect.Sync
import cats.implicits._
import domain.adt.MaintenanceStatus
import persistence.command.CommandResponse
import doobie.util.transactor.Transactor
import doobie.implicits._
import persistence.model.Maintenance
import persistence.model.Maintenance._
import domain.adt.MaintenanceStatus._
import doobie.postgres.implicits._

import java.util.UUID
import java.time.LocalDateTime

trait MaintenanceRepositoryAPI[F[_]] {
  def create(maintenance: Maintenance): F[CommandResponse[Maintenance]]
  def get(id: UUID): F[CommandResponse[Maintenance]]
  def getAll: F[CommandResponse[Map[UUID, Maintenance]]]
  def schedule(id: UUID, scheduledDate: LocalDateTime): F[CommandResponse[Maintenance]]
}

class MaintenanceRepository[F[_]: Sync](xa: Transactor[F]) extends MaintenanceRepositoryAPI[F] {

  override def create(maintenance: Maintenance): F[CommandResponse[Maintenance]] = {
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
      .map[CommandResponse[Maintenance]](_ => CommandResponse.Success(Some(maintenance)))
      .handleErrorWith(err => Sync[F].pure(CommandResponse.Failure(err.getMessage)))
  }

  override def get(id: UUID): F[CommandResponse[Maintenance]] = sql"""
      SELECT id, car_id, description, maintenance_types, scheduled_date,
             status, created_at, updated_at
      FROM maintenances
      WHERE id = $id
    """.query[Maintenance].option.transact(xa).map {
    case Some(maintenance) => CommandResponse.Success(Some(maintenance))
    case None              => CommandResponse.Failure("Maintenance not found")
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
    .map(maintenances => CommandResponse.Success(Some(maintenances.map(m => m.id -> m).toMap)))

  override def schedule(id: UUID, scheduledDate: LocalDateTime): F[CommandResponse[Maintenance]] = {
    val now                                = LocalDateTime.now()
    val scheduledStatus: MaintenanceStatus = MaintenanceStatus.Scheduled
    sql"""
      UPDATE maintenances
      SET status = $scheduledStatus, scheduled_date = $scheduledDate, updated_at = $now
      WHERE id = $id
      RETURNING id, car_id, description, maintenance_types, scheduled_date,
                status, created_at, updated_at
    """.query[Maintenance].option.transact(xa).map {
      case Some(maintenance) => CommandResponse.Success(Some(maintenance))
      case None              => CommandResponse.Failure("Maintenance not found")
    }
  }
}
