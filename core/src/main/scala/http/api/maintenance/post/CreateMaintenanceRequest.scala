package http.api.maintenance.post

import java.time.LocalDateTime
import java.util.UUID

case class CreateMaintenanceRequest(
  carId: UUID,
  maintenanceTypes: List[UUID],
  description: String,
  scheduledDate: LocalDateTime
)
