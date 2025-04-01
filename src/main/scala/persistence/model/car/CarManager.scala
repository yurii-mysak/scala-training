package persistence.model.car

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import persistence.command.CommandResponse
import persistence.command.car.Command
import persistence.event.car.Event
import persistence.event.car.Event.Created
import persistence.model.State

import java.util.UUID

object CarManager {
  private case class ManagerState(actors: Map[UUID, ActorRef[Command]])

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    EventSourcedBehavior[Command, Event, State[ManagerState]](
      persistenceId = PersistenceId.ofUniqueId("car-manager"),
      emptyState = State(Some(ManagerState(Map.empty))),
      commandHandler = handleCommand(context),
      eventHandler = handleEvent(context)
    )
  }

  // todo: how to work with such signatures?
  private def handleCommand(context: ActorContext[Command]): (State[ManagerState], Command) => Effect[Event, State[ManagerState]] = (state, command) => {
    command match {
      case Command.Get(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) =>
        val carActors = state.state.getOrElse(ManagerState(Map.empty)).actors
        Effect.none
          .thenRun(_ =>
            carActors.get(id) match {
            case Some(ref) =>
              ref ! Command.Get(id, replyTo)
            case None =>
              replyTo ! CommandResponse.Failure("Car not found")
          })

      case createCar @ Command.Create(car, _) =>
        val ref = context.spawn(Car(car.id), s"car-${car.id}")

        Effect
          .persist(Created(car))
          .thenReply(ref)(_ => createCar)
    }
  }

  private def handleEvent(context: ActorContext[Command]): (State[ManagerState], Event) => State[ManagerState] = (state, event) =>
    event match {
      case Created(car) =>
        val carActorId = s"car-${car.id}"
        val carActorRef = context.child(carActorId) // exists after command handler,
          .getOrElse(context.spawn(Car(car.id), carActorId))
          .unsafeUpcast[AnyRef] // todo: why unsafeUpcast and narrow, any other approaches?
          .narrow[Command]
        val carActors = state.state.getOrElse(ManagerState(Map.empty)).actors
        State(Some(ManagerState(carActors + (car.id -> carActorRef))))
    }
}