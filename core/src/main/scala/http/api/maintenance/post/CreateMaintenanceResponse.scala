package http.api.maintenance.post

import domain.model.MaintenanceCore
import persistence.command.CommandResponse

import java.util.UUID

case class CreateMaintenanceResponse[A <: MaintenanceCore](id: UUID, status: String)

object CreateMaintenanceResponse {

  implicit def toCreateMaintenanceResponse[A <: MaintenanceCore](
    response: CommandResponse[A]
  ): CreateMaintenanceResponse[A] = response match {
    case CommandResponse.Success(Some(maintenance: MaintenanceCore)) =>
      CreateMaintenanceResponse(maintenance.id, maintenance.status.value)
    case CommandResponse.Success(None)                               =>
      throw new IllegalStateException("Maintenance creation succeeded but no maintenance was returned")
    case CommandResponse.Failure(reason)                             =>
      throw new IllegalStateException(reason)
  }
}
