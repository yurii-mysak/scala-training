package persistence.command.maintenance

import akka.actor.typed.ActorRef
import domain.adt.{MaintenanceStatus, MaintenanceType}
import persistence.command.CommandResponse
import persistence.model.maintenance.Maintenance

import java.util.UUID
import java.time.LocalDateTime

sealed trait Command
object Command {
  case class Schedule(carId: UUID, description: String, maintenanceType: MaintenanceType, scheduledDate: LocalDateTime, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command
  case class UpdateStatus(id: UUID, status: MaintenanceStatus, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command
  case class Reschedule(id: UUID, scheduledDate: LocalDateTime, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command
  case class Get(id: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command
  case class Cancel(id: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command
}