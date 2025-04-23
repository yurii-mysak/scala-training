package com.scala_training.akka.persistence.event.maintenance

import akka.serialization.jackson.JsonSerializable
import com.scala_training.akka.persistence.model.maintenance.Maintenance

import java.util.UUID

sealed trait Event extends JsonSerializable

object Event {

  case class Created(maintenance: Maintenance) extends Event

  case class Updated(maintenance: Maintenance) extends Event

  case class Deleted(id: UUID) extends Event

  case class Scheduled(maintenance: Maintenance) extends Event

  case class StatusUpdated(maintenance: Maintenance) extends Event

  case class Cancelled(id: UUID) extends Event
}
