package com.scala_training.akka.persistence.repository

import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.scala_training.akka.persistence.command.car.Command
import com.scala_training.akka.persistence.model.car.Car
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class CarRepository(carManager: ActorRef[Command])(
  implicit ec: ExecutionContext,
  timeout: Timeout,
  system: ActorSystem[?]
) {

  def getAll: Future[CommandResponse[Map[UUID, Car]]] =
    carManager.ask[CommandResponse[Map[UUID, Car]]](replyTo => Command.GetAll(replyTo))

  def getById(id: UUID): Future[CommandResponse[Car]] =
    carManager.ask[CommandResponse[Car]](replyTo => Command.Get(id, replyTo))

  def create(car: Car): Future[CommandResponse[Car]] =
    carManager.ask[CommandResponse[Car]](replyTo => Command.Create(car, replyTo))
}
