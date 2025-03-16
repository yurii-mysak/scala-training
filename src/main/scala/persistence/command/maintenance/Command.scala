package com.github.scala_training
package persistence.command.maintenance

import akka.actor.typed.ActorRef
import com.github.scala_training.domain.enums.{MaintenanceStatus, MaintenanceType}
import com.github.scala_training.persistence.command.CommandResponse
import com.github.scala_training.persistence.model.maintenance.Maintenance

import java.util.UUID
import java.time.LocalDateTime

// Commands
enum Command {
  case Schedule(carId: UUID, description: String, maintenanceType: MaintenanceType, scheduledDate: LocalDateTime, replyTo: akka.actor.typed.ActorRef[CommandResponse[Maintenance]])
  case UpdateStatus(id: UUID, status: MaintenanceStatus, replyTo: akka.actor.typed.ActorRef[CommandResponse[Maintenance]])
  case Reschedule(id: UUID, scheduledDate: LocalDateTime, replyTo: akka.actor.typed.ActorRef[CommandResponse[Maintenance]])
  case Get(id: UUID, replyTo: akka.actor.typed.ActorRef[CommandResponse[Maintenance]])
  case Cancel(id: UUID, replyTo: akka.actor.typed.ActorRef[CommandResponse[Maintenance]])
}