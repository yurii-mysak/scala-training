package com.github.scala_training
package persistence.event.car

import akka.actor.typed.ActorRef
import com.github.scala_training.persistence.model.car.Car

import java.util.UUID

// Events
enum Event {
  case Created(car: Car)
  case Updated(car: Car)
  case Deleted(id: UUID)
}