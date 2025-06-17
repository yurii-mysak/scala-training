package com.scala_training.akka.persistence.command.car

import akka.actor.typed.ActorRef
import com.scala_training.akka.persistence.model.car.Car
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID

sealed trait Command

object Command {
  case object NoOp extends Command

  case class Create(car: Car, replyTo: ActorRef[CommandResponse[Car]]) extends Command

  case class Get(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) extends Command

  case class Delete(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) extends Command

  case class GetAll(replyTo: ActorRef[CommandResponse[Map[UUID, Car]]]) extends Command
}
