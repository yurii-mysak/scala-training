package persistence.repository

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import persistence.command.CommandResponse
import persistence.command.car.Command
import persistence.model.car.Car

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class CarRepository(carManager: ActorRef[Command])(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem[?]) {

  def getAll: Future[CommandResponse[Map[UUID, Car]]] = {
    carManager.ask[CommandResponse[Map[UUID, Car]]](replyTo => Command.GetAll(replyTo))
  }

  def getById(id: UUID): Future[CommandResponse[Car]] = {
    carManager.ask[CommandResponse[Car]](replyTo => Command.Get(id, replyTo))
  }

  def create(car: Car): Future[CommandResponse[Car]] = {
    carManager.ask[CommandResponse[Car]](replyTo => Command.Create(car, replyTo))
  }
}