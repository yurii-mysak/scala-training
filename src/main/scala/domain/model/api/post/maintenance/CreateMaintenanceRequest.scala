package com.github.scala_training
package domain.model.api.post.maintenance

import domain.enums.MaintenanceType

import java.util.UUID

final case class CreateMaintenanceRequest(carId: UUID, maintenanceType: MaintenanceType, description: String, scheduledDate: String)
