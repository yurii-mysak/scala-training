package persistence.event.car

import akka.serialization.jackson.JsonSerializable
import persistence.model.car.Car

import java.util.UUID

sealed trait Event extends JsonSerializable

object Event {
  case class Created(car: Car) extends Event

  case class Updated(car: Car) extends Event

  case class Deleted(id: UUID) extends Event
}
