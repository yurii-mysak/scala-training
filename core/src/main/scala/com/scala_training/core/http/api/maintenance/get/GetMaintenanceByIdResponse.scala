package com.scala_training.core.http.api.maintenance.get

import com.scala_training.core.domain.model.MaintenanceCore
import com.scala_training.core.persistence.command.CommandResponse

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

  given [A <: MaintenanceCore]: Conversion[CommandResponse[A], GetMaintenanceByIdResponse[A]] with {

    def apply(response: CommandResponse[A]): GetMaintenanceByIdResponse[A] = response match {
      case CommandResponse.Success(Some(maintenance: MaintenanceCore)) =>
        GetMaintenanceByIdResponse(
          maintenance.id,
          maintenance.carId,
          maintenance.maintenanceTypes.map(maintenanceType => maintenanceType.id),
          maintenance.status.value,
          maintenance.description,
          maintenance.scheduledDate
            .getOrElse(throw new IllegalStateException(s"Incorrect state of maintenance ${maintenance.id}"))
            .toString
        )
      case CommandResponse.Success(None)                               =>
        throw new IllegalStateException(s"Maintenance was not found")
      case CommandResponse.Failure(reason)                             =>
        throw new IllegalStateException(reason)
    }
  }
}
