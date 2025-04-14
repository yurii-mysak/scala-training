package application

import cats.effect.Sync
import persistence.command.CommandResponse
import persistence.model.Maintenance
import persistence.repository.MaintenanceRepositoryAPI

import java.time.LocalDateTime
import java.util.UUID

class MaintenanceService[F[_]: Sync](repository: MaintenanceRepositoryAPI[F]) {

  def createMaintenance(maintenance: Maintenance): F[CommandResponse[Maintenance]] = repository.create(maintenance)

  def getMaintenance(id: UUID): F[CommandResponse[Maintenance]] = repository.get(id)

  def getAllMaintenances: F[CommandResponse[Map[UUID, Maintenance]]] = repository.getAll

  def scheduleMaintenance(id: UUID, time: LocalDateTime): F[CommandResponse[Maintenance]] =
    repository.schedule(id, time)
}
