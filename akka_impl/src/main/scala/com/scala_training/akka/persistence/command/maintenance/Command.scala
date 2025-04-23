package com.scala_training.akka.persistence.command.maintenance

import akka.actor.typed.ActorRef
import com.scala_training.akka.persistence.model.maintenance.Maintenance
import com.scala_training.core.domain.adt.MaintenanceStatus
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID

sealed trait Command

object Command {
  case class Create(maintenance: Maintenance, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class Schedule(maintenance: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class UpdateStatus(id: UUID, status: MaintenanceStatus, replyTo: ActorRef[CommandResponse[Maintenance]])
    extends Command

  case class Get(id: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) extends Command

  case class GetAll(replyTo: ActorRef[CommandResponse[Map[UUID, Maintenance]]]) extends Command
}
