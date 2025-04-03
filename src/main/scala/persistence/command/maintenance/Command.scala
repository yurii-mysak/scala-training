package persistence.command.maintenance

import akka.actor.typed.ActorRef
import domain.adt.{MaintenanceStatus, MaintenanceType}
import persistence.command.CommandResponse
import persistence.model.maintenance.Maintenance

import java.util.UUID
import java.time.LocalDateTime

sealed trait Command

object Command {
  case class Create(maintenance: Maintenance, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class Schedule(maintenance: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class UpdateStatus(id: UUID, status: MaintenanceStatus, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class Get(id: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class GetAll(replyTo: ActorRef[CommandResponse[Map[UUID, Maintenance]]]) extends Command
}