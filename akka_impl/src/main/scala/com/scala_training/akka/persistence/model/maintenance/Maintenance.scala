package com.scala_training.akka.persistence.model.maintenance

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}

import java.time.LocalDateTime
import java.util.UUID
import akka.serialization.jackson.JsonSerializable
import com.scala_training.akka.persistence.command.maintenance.Command
import com.scala_training.akka.persistence.event.maintenance.Event
import com.scala_training.akka.persistence.model.State
import com.scala_training.core.domain.adt.{MaintenanceStatus, MaintenanceType}
import com.scala_training.core.domain.model.MaintenanceCore
import com.scala_training.core.persistence.command.CommandResponse

case class Maintenance(
  id: UUID,
  carId: UUID,
  description: String,
  maintenanceTypes: List[MaintenanceType],
  scheduledDate: Option[LocalDateTime] = None,
  status: MaintenanceStatus,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime] = None
) extends MaintenanceCore
  with JsonSerializable

object Maintenance {

  def apply(id: UUID): Behavior[Command] = Behaviors.setup { _ =>
    EventSourcedBehavior(
      persistenceId = PersistenceId.ofUniqueId(s"maintenance-$id"),
      emptyState = State(None),
      commandHandler = handleCommand,
      eventHandler = handleEvent
    ).withRetention(
      RetentionCriteria.snapshotEvery(numberOfEvents = 5, keepNSnapshots = 3)
    )
  }

  private def handleCommand(
    state: State[Maintenance],
    command: Command
  ): akka.persistence.typed.scaladsl.Effect[Event, State[Maintenance]] = {
    import Command._

    command match {
      case Create(maintenance, replyTo) =>
        if (state.state.isDefined)
          akka.persistence.typed.scaladsl.Effect.none
            .thenReply(replyTo)(_ => CommandResponse.Failure("Maintenance already exists"))
        else {
          akka.persistence.typed.scaladsl.Effect
            .persist(Event.Created(maintenance))
            .thenReply(replyTo)(_ => CommandResponse.Success(Some(maintenance)))
        }

      case Get(_, replyTo) =>
        akka.persistence.typed.scaladsl.Effect.reply(replyTo)(CommandResponse.Success(state.state))

      case Schedule(_, replyTo) =>
        state.state match {
          case Some(maintenance) =>
            val scheduledMaintenance = maintenance.copy(
              status = MaintenanceStatus.Scheduled,
              updatedAt = Some(LocalDateTime.now())
            )
            akka.persistence.typed.scaladsl.Effect
              .persist(Event.Scheduled(scheduledMaintenance))
              .thenReply(replyTo)(_ => CommandResponse.Success(Some(scheduledMaintenance)))
          case None              =>
            akka.persistence.typed.scaladsl.Effect.reply(replyTo)(CommandResponse.Failure("Maintenance not found"))
        }

      case _ => akka.persistence.typed.scaladsl.Effect.none
    }
  }

  private def handleEvent(state: State[Maintenance], event: Event): State[Maintenance] = {
    import Event._

    event match {
      case Created(maintenance)       => State(Some(maintenance))
      case StatusUpdated(maintenance) => State(Some(maintenance))
      case Updated(maintenance)       => State(Some(maintenance))
      case Scheduled(maintenance)     => State(Some(maintenance))
      case Cancelled(_)               => State(None)
      case Deleted(_)                 => State(None)
    }
  }
}
