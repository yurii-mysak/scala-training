package com.scala_training.akka.persistence.repository

import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.scala_training.akka.persistence.command.maintenance.Command
import com.scala_training.akka.persistence.model.maintenance.Maintenance
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MaintenanceRepository(maintenanceManager: ActorRef[Command])(
  implicit ec: ExecutionContext,
  timeout: Timeout,
  system: ActorSystem[?]
) {

  def getAll: Future[CommandResponse[Map[UUID, Maintenance]]] =
    maintenanceManager.ask[CommandResponse[Map[UUID, Maintenance]]](replyTo => Command.GetAll(replyTo))

  def getById(id: UUID): Future[CommandResponse[Maintenance]] =
    maintenanceManager.ask[CommandResponse[Maintenance]](replyTo => Command.Get(id, replyTo))

  def create(Maintenance: Maintenance): Future[CommandResponse[Maintenance]] =
    maintenanceManager.ask[CommandResponse[Maintenance]](replyTo => Command.Create(Maintenance, replyTo))

  def schedule(id: UUID): Future[CommandResponse[Maintenance]] =
    maintenanceManager.ask[CommandResponse[Maintenance]](replyTo => Command.Schedule(id, replyTo))
}
