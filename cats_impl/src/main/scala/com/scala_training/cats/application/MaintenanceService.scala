package com.scala_training.cats.application

import cats.effect.Concurrent
import com.scala_training.cats.persistence.model.Maintenance
import com.scala_training.cats.persistence.repository.MaintenanceRepositoryAPI
import com.scala_training.core.domain.adt.{MaintenanceStatus, MaintenanceType}
import com.scala_training.core.http.api.maintenance.post.CreateMaintenanceRequest
import com.scala_training.core.persistence.command.CommandResponse

import java.time.LocalDateTime
import java.util.UUID

class MaintenanceService[F[_]: Concurrent](repository: MaintenanceRepositoryAPI[F]) {

  private def createMaintenance(maintenance: Maintenance): F[CommandResponse[Maintenance]] =
    repository.create(maintenance)

  def createMaintenance(
    req: CreateMaintenanceRequest,
    types: List[MaintenanceType]
  ): F[CommandResponse[Maintenance]] = {
    val now         = LocalDateTime.now()
    val maintenance = Maintenance(
      UUID.randomUUID(),
      req.carId,
      req.description,
      types,
      Some(req.scheduledDate),
      MaintenanceStatus.Created,
      now,
      Some(now)
    )

    this.createMaintenance(maintenance)
  }

  def getMaintenance(id: UUID): F[CommandResponse[Maintenance]] = repository.get(id)

  def getAllMaintenances: F[CommandResponse[Map[UUID, Maintenance]]] = repository.getAll

  def scheduleMaintenance(id: UUID, time: LocalDateTime): F[CommandResponse[Maintenance]] =
    repository.schedule(id, time)
}
