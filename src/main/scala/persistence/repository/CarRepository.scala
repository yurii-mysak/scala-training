package com.github.scala_training.persistence.repository

import com.github.scala_training.persistence.model.car.Car
import com.github.scala_training.persistence.command.car.Command
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import com.github.scala_training.persistence.command.CommandResponse

import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

class CarRepository(carActor: ActorRef[Command])(using ec: ExecutionContext, timeout: Timeout, system: ActorSystem[?]) {

  def getAll: Future[Seq[CommandResponse[Car]]] = {
    carActor.ask[Seq[CommandResponse[Car]]](replyTo => Command.GetAll(replyTo))
  }

  def getById(id: UUID): Future[Option[CommandResponse[Car]]] = {
    carActor.ask[Option[CommandResponse[Car]]](replyTo => Command.Get(id, replyTo))
  }

  def create(car: Car): Future[CommandResponse[Car]] = {
    carActor.ask[CommandResponse[Car]](replyTo => Command.Create(car.make, car.model, car.year, replyTo))
  }
}