package com.github.scala_training
package persistence.event.maintenance

import akka.actor.typed.ActorRef
import com.github.scala_training.persistence.model.maintenance.Maintenance

import java.util.UUID

// Events
enum Event {
  case Scheduled(maintenance: Maintenance)
  case StatusUpdated(maintenance: Maintenance)
  case Rescheduled(maintenance: Maintenance)
  case Cancelled(id: UUID)
}