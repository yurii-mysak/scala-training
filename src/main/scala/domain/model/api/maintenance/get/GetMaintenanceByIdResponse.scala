package domain.model.api.maintenance.get

import persistence.command.CommandResponse
import persistence.model.maintenance.Maintenance

import java.util.UUID

case class GetMaintenanceByIdResponse(
                                       id: UUID,
                                       carId: UUID,
                                       maintenanceTypes: List[UUID],
                                       status: String,
                                       description: String,
                                       scheduledDate: String
                                     ) {}

object GetMaintenanceByIdResponse {
  implicit def toGetMaintenanceByIdResponse(response: CommandResponse[Maintenance]): GetMaintenanceByIdResponse = {
    response match {
      case CommandResponse.Success(Some(maintenance: Maintenance)) =>
        GetMaintenanceByIdResponse(
          maintenance.id,
          maintenance.carId,
          maintenance.maintenanceTypes.map(maintenanceType => maintenanceType.id),
          maintenance.status.toString,
          maintenance.description,
          maintenance.scheduledDate.toString
        )
      case CommandResponse.Success(None) =>
        throw new IllegalStateException(s"Maintenance was not found")
      case CommandResponse.Failure(reason) =>
        throw new IllegalStateException(reason)
    }
  }
}