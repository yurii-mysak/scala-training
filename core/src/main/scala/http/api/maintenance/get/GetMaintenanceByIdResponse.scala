package http.api.maintenance.get

import domain.model.MaintenanceCore
import persistence.command.CommandResponse

import java.util.UUID

case class GetMaintenanceByIdResponse[A <: MaintenanceCore](
  id: UUID,
  carId: UUID,
  maintenanceTypes: List[UUID],
  status: String,
  description: String,
  scheduledDate: String
) {}

object GetMaintenanceByIdResponse {

  implicit def toGetMaintenanceByIdResponse[A <: MaintenanceCore](
    response: CommandResponse[A]
  ): GetMaintenanceByIdResponse[A] = response match {
    case CommandResponse.Success(Some(maintenance: MaintenanceCore)) =>
      GetMaintenanceByIdResponse(
        maintenance.id,
        maintenance.carId,
        maintenance.maintenanceTypes.map(maintenanceType => maintenanceType.id),
        maintenance.status.value,
        maintenance.description,
        maintenance.scheduledDate.toString
      )
    case CommandResponse.Success(None)                               =>
      throw new IllegalStateException(s"Maintenance was not found")
    case CommandResponse.Failure(reason)                             =>
      throw new IllegalStateException(reason)
  }
}
