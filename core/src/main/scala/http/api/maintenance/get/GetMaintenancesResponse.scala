package http.api.maintenance.get

import domain.model.MaintenanceCore
import persistence.command.CommandResponse

import java.util.UUID

case class GetMaintenancesResponse[A <: MaintenanceCore](maintenances: Map[UUID, GetMaintenanceByIdResponse[A]])

object GetMaintenancesResponse {

  implicit def toGetMaintenancesResponse[A <: MaintenanceCore](
    response: CommandResponse[Map[UUID, A]]
  ): GetMaintenancesResponse[A] = response match {
    case CommandResponse.Success(Some(maintenances: Map[UUID, MaintenanceCore])) =>
      GetMaintenancesResponse(maintenances.map { case (id, maintenance) =>
        id -> GetMaintenanceByIdResponse(
          maintenance.id,
          maintenance.carId,
          maintenance.maintenanceTypes.map(maintenanceType => maintenanceType.id),
          maintenance.status.value,
          maintenance.description,
          maintenance.scheduledDate.toString
        )
      })

    case CommandResponse.Failure(reason) =>
      throw new IllegalStateException(reason)
  }
}
