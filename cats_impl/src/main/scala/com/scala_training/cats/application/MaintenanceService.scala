package com.scala_training.cats.application

import cats.effect.Concurrent
import com.scala_training.cats.persistence.model.Maintenance
import com.scala_training.cats.persistence.repository.MaintenanceRepositoryAPI
import com.scala_training.core.persistence.command.CommandResponse

import java.time.LocalDateTime
import java.util.UUID

class MaintenanceService[F[_]: Concurrent](repository: MaintenanceRepositoryAPI[F]) {

  def createMaintenance(maintenance: Maintenance): F[CommandResponse[Maintenance]] = repository.create(maintenance)

  def getMaintenance(id: UUID): F[CommandResponse[Maintenance]] = repository.get(id)

  def getAllMaintenances: F[CommandResponse[Map[UUID, Maintenance]]] = repository.getAll

  def scheduleMaintenance(id: UUID, time: LocalDateTime): F[CommandResponse[Maintenance]] =
    repository.schedule(id, time)
}
