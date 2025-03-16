package com.github.scala_training
package domain.model.api.post.maintenance

import domain.enums.MaintenanceStatus
import java.util.UUID

final case class CreateMaintenanceResponse(id: UUID, status: MaintenanceStatus)
