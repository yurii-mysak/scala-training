package domain.adt

sealed trait MaintenanceType
object MaintenanceType {
  case object OilChange extends MaintenanceType
  case object TireRotation extends MaintenanceType
  case object BrakeCheck extends MaintenanceType
  case object EngineCheck extends MaintenanceType
  case object BatteryCheck extends MaintenanceType
  case object TransmissionCheck extends MaintenanceType
  case object RadiatorCheck extends MaintenanceType
  case object AirConditioningCheck extends MaintenanceType
  case object Other extends MaintenanceType
}