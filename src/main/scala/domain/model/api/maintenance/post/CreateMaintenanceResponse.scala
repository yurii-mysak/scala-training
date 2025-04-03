package domain.model.api.maintenance.post

import domain.adt.MaintenanceStatus
import persistence.command.CommandResponse
import persistence.model.maintenance.Maintenance

import java.util.UUID

case class CreateMaintenanceResponse(id: UUID, status: String)

object CreateMaintenanceResponse {
  implicit def toCreateMaintenanceResponse(response: CommandResponse[Maintenance]): CreateMaintenanceResponse = {
    response match {
      case CommandResponse.Success(Some(maintenance: Maintenance)) =>
        CreateMaintenanceResponse(maintenance.id, maintenance.status.toString)
      case CommandResponse.Success(None) =>
        throw new IllegalStateException("Maintenance creation succeeded but no maintenance was returned")
      case CommandResponse.Failure(reason) =>
        throw new IllegalStateException(reason)
    }
  }
}