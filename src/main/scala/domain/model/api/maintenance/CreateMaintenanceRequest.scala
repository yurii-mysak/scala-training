package domain.model.api.maintenance

import domain.adt.MaintenanceType

import java.util.UUID

case class CreateMaintenanceRequest(carId: UUID, maintenanceType: MaintenanceType, description: String, scheduledDate: String)
