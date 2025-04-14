package persistence.model.car

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import persistence.model.car.Car
import persistence.model.car.{Car => CarActor}
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

  private def handleCommand(
    context: ActorContext[Command]
  )(state: State[ManagerState], command: Command): Effect[Event, State[ManagerState]] = command match {
    case Command.Get(id: UUID, replyTo: ActorRef[CommandResponse[Car]]) =>
      val carActors = state.state.getOrElse(ManagerState(Map.empty)).actors
      Effect.none
        .thenRun(_ =>
          carActors.get(id) match {
            case Some(ref) =>
              ref ! Command.Get(id, replyTo)
            case None      =>
              replyTo ! CommandResponse.Failure("Car not found")
          }
        )

    case Command.GetAll(replyTo: ActorRef[CommandResponse[Map[UUID, Car]]]) =>
      val carActors = state.state.getOrElse(ManagerState(Map.empty)).actors

      Effect.none
        .thenRun { _ =>
          import context.executionContext

          import scala.concurrent.Future
          import scala.concurrent.duration._
          implicit val timeout: Timeout = 3.seconds

          if (carActors.isEmpty) {
            replyTo ! CommandResponse.Success(Some(Map.empty))
          } else {
            val futures = carActors.map { case (id, actor) =>
              actor
                .ask[CommandResponse[Car]](ref => Command.Get(id, ref))(timeout, context.system.scheduler)
                .map(response => id -> response)
            }

            Future.sequence(futures).foreach { responses =>
              val result = responses.collect { case (id, CommandResponse.Success(Some(car))) =>
                id -> car
              }.toMap

              replyTo ! CommandResponse.Success(Some(result))
            }
          }
        }

    case createCar @ Command.Create(car, _) =>
      val ref = context.spawn(CarActor(car.id), s"car-${car.id}")

      Effect
        .persist(Created(car))
        .thenReply(ref)(_ => createCar)
  }

  private def handleEvent(
    context: ActorContext[Command]
  )(state: State[ManagerState], event: Event): State[ManagerState] = event match {
    case Created(car) =>
      val carActorId  = s"car-${car.id}"
      val carActorRef = context
        .child(carActorId) // exists after command handler,
        .getOrElse(context.spawn(CarActor(car.id), carActorId))
        .unsafeUpcast[AnyRef]
        .narrow[Command]
      val carActors   = state.state.getOrElse(ManagerState(Map.empty)).actors
      State(Some(ManagerState(carActors + (car.id -> carActorRef))))
  }
}
