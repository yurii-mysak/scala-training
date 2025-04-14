package persistence.command.car

import akka.actor.typed.ActorRef
import persistence.model.car.Car
import persistence.command.CommandResponse

import java.util.UUID

sealed trait Command

object Command {
  case class Create(car: Car, replyTo: ActorRef[CommandResponse[Car]]) extends Command

  case class Get(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) extends Command

  case class Delete(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) extends Command

  case class GetAll(replyTo: ActorRef[CommandResponse[Map[UUID, Car]]]) extends Command
}
