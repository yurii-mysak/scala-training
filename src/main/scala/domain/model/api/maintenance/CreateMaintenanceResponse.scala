package domain.model.api.maintenance

import domain.adt.MaintenanceStatus
import java.util.UUID

case class CreateMaintenanceResponse(id: UUID, status: MaintenanceStatus)
