package com.github.scala_training
package persistence.command.car

import akka.actor.typed.ActorRef
import com.github.scala_training.persistence.command.CommandResponse
import com.github.scala_training.persistence.model.car.Car

import java.util.UUID

enum Command {
  case Create(make: String, model: String, year: Int, replyTo: ActorRef[CommandResponse[Car]])
  case Update(id: UUID, make: String, model: String, year: Int, replyTo: ActorRef[CommandResponse[Car]])
  case Get(id: UUID, replyTo: ActorRef[Option[CommandResponse[Car]]])
  case Delete(id: UUID, replyTo: ActorRef[CommandResponse[Car]])
  case GetAll(replyTo: ActorRef[Seq[CommandResponse[Car]]])
}