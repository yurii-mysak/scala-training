package persistence.model.car

import persistence.command.car.Command
import persistence.event.car.Event
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import persistence.command.CommandResponse
import persistence.model.State

import java.time.LocalDateTime
import java.util.UUID
import java.time.Year

case class Car(
                id: UUID,
                make: String,
                model: String,
                year: Year,
                createdAt: LocalDateTime,
                updatedAt: Option[LocalDateTime] = None
              )

object Car {
  def apply(id: UUID): Behavior[Command] = {
    Behaviors.setup { context =>
      EventSourcedBehavior(
        persistenceId = PersistenceId.ofUniqueId(s"car-$id"),
        emptyState = State(None),
        commandHandler = handleCommand,
        eventHandler = handleEvent
      ).withRetention(
        RetentionCriteria.snapshotEvery(numberOfEvents = 5, keepNSnapshots = 3)
      )
    }
  }

  private def handleCommand(
                             state: State[Car],
                             command: Command
                           ): akka.persistence.typed.scaladsl.Effect[Event, State[Car]] = {
    import Command._

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
    import Event._

    event match {
      case Created(car) => State(Some(car))
      case Updated(car) => State(Some(car)) // todo: improve when car update is possible + use state
      case Deleted(_) => State(None)
    }
  }
}