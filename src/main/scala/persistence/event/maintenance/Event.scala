package persistence.event.maintenance

import persistence.model.maintenance.Maintenance
import java.util.UUID

sealed trait Event
object Event {
  case class Scheduled(maintenance: Maintenance) extends Event
  case class StatusUpdated(maintenance: Maintenance) extends Event
  case class Rescheduled(maintenance: Maintenance) extends Event
  case class Cancelled(id: UUID) extends Event
}