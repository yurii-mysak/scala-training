package com.scala_training.core.domain.model

import com.scala_training.core.domain.adt.{MaintenanceStatus, MaintenanceType}

import java.time.LocalDateTime
import java.util.UUID

trait MaintenanceCore {
  val id: UUID
  val carId: UUID
  val description: String
  val maintenanceTypes: List[MaintenanceType]
  val scheduledDate: Option[LocalDateTime]
  val status: MaintenanceStatus
  val createdAt: LocalDateTime
  val updatedAt: Option[LocalDateTime]
}
