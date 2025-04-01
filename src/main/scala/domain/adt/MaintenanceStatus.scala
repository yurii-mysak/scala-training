package domain.adt

sealed trait MaintenanceStatus
object MaintenanceStatus {
  case object Created extends MaintenanceStatus
  case object Scheduled extends MaintenanceStatus
  case object Pending extends MaintenanceStatus
  case object InProgress extends MaintenanceStatus
  case object Completed extends MaintenanceStatus
  case object Cancelled extends MaintenanceStatus
}