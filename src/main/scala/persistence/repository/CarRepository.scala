package persistence.repository

import persistence.model.car.{Car, CarManager}
import persistence.command.car.Command
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import persistence.command.CommandResponse

import scala.concurrent.{ExecutionContext, Future, Promise}
import java.util.UUID

class CarRepository(carManager: ActorRef[Command])(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem[?]) {

//  def getAll: Future[Seq[CommandResponse[Car]]] = {
////    carActor.ask[Seq[CommandResponse[Car]]](replyTo => Command.GetAll(replyTo))
//  }

  def getById(id: UUID): Future[CommandResponse[Car]] = {
    carManager.ask[CommandResponse[Car]](replyTo => Command.Get(id, replyTo))
  }

  def create(car: Car): Future[CommandResponse[Car]] = {
    carManager.ask[CommandResponse[Car]](replyTo => Command.Create(car, replyTo))
  }
}