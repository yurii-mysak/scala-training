package com.scala_training.akka.persistence.model.car

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}

import java.util.UUID
import akka.serialization.jackson.JsonSerializable
import com.scala_training.akka.persistence.command.car.Command
import com.scala_training.akka.persistence.event.car.Event
import com.scala_training.akka.persistence.model.State
import com.scala_training.core.domain.model.CarCore
import com.scala_training.core.persistence.command.CommandResponse

import java.time.{LocalDateTime, Year}

case class Car(
  id: UUID,
  make: String,
  model: String,
  year: Year,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime] = None
) extends CarCore
  with JsonSerializable

object Car {

  def apply(id: UUID): Behavior[Command] = Behaviors.setup { _ =>
    EventSourcedBehavior(
      persistenceId = PersistenceId.ofUniqueId(s"car-$id"),
      emptyState = State(None),
      commandHandler = handleCommand,
      eventHandler = handleEvent
    ).withRetention(
      RetentionCriteria.snapshotEvery(numberOfEvents = 5, keepNSnapshots = 3)
    )
  }

  private def handleCommand(
    state: State[Car],
    command: Command
  ): akka.persistence.typed.scaladsl.Effect[Event, State[Car]] = {
    import Command.*

    command match {
      case Create(car, replyTo) =>
        if (state.state.isDefined)
          akka.persistence.typed.scaladsl.Effect.none
            .thenReply(replyTo)(_ => CommandResponse.Failure("Car already exists"))
        else {
          akka.persistence.typed.scaladsl.Effect
            .persist(Event.Created(car))
            .thenReply(replyTo)(_ => CommandResponse.Success(Some(car)))
        }

      case Get(_, replyTo) =>
        akka.persistence.typed.scaladsl.Effect.reply(replyTo)(CommandResponse.Success(state.state))

      // Implement other command handlers here
      case _ => akka.persistence.typed.scaladsl.Effect.none
    }
  }

  private def handleEvent(state: State[Car], event: Event): State[Car] = {
    import Event.*

    event match {
      case Created(car) => State(Some(car))
      case Updated(car) => State(Some(car)) // todo: improve when car update is possible + use state
      case Deleted(_)   => State(None)
    }
  }
}
