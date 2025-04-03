package domain.model.api.maintenance.get

import persistence.command.CommandResponse
import persistence.model.maintenance.Maintenance

import java.util.UUID

case class GetMaintenancesResponse(maintenances: Map[UUID, GetMaintenanceByIdResponse])


object GetMaintenancesResponse {
  implicit def toGetMaintenancesResponse(response: CommandResponse[Map[UUID, Maintenance]]): GetMaintenancesResponse = {
    response match {
      case CommandResponse.Success(Some(maintenances: Map[UUID, Maintenance])) =>
        GetMaintenancesResponse(maintenances.map {
          case (id, maintenance) => id -> GetMaintenanceByIdResponse(
            maintenance.id,
            maintenance.carId,
            maintenance.maintenanceTypes.map(maintenanceType => maintenanceType.id),
            maintenance.status.toString,
            maintenance.description,
            maintenance.scheduledDate.toString
          )
        })

      case CommandResponse.Failure(reason) =>
        throw new IllegalStateException(reason)
    }
  }
}