package persistence.command.car

import akka.actor.typed.ActorRef
import persistence.command.CommandResponse
import persistence.model.car.Car

import java.util.UUID
import java.time.Year

sealed trait Command
object Command {
  case class Create(car: Car, replyTo: ActorRef[CommandResponse[Car]]) extends Command
  case class Update(id: UUID, make: String, model: String, year: Year, replyTo: ActorRef[CommandResponse[Car]]) extends Command
  case class Get(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) extends Command
  case class Delete(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) extends Command
  case class GetAll(replyTo: ActorRef[Seq[CommandResponse[Car]]]) extends Command
}