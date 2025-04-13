package persistence.model.maintenance

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import persistence.model.maintenance.{Maintenance => MaintenanceActor}
import persistence.command.CommandResponse
import persistence.command.maintenance.Command
import persistence.event.maintenance.Event
import persistence.event.maintenance.Event.Created
import persistence.model.State

import java.util.UUID

object MaintenanceManager {
  private case class ManagerState(actors: Map[UUID, ActorRef[Command]])

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    EventSourcedBehavior[Command, Event, State[ManagerState]](
      persistenceId = PersistenceId.ofUniqueId("maintenance-manager"),
      emptyState = State(Some(ManagerState(Map.empty))),
      commandHandler = handleCommand(context),
      eventHandler = handleEvent(context)
    )
  }

  private def handleCommand(
    context: ActorContext[Command]
  )(state: State[ManagerState], command: Command): Effect[Event, State[ManagerState]] = command match {
    case Command.Get(id: UUID, replyTo: ActorRef[CommandResponse[Maintenance]]) =>
      val maintenanceActors = state.state.getOrElse(ManagerState(Map.empty)).actors
      Effect.none
        .thenRun(_ =>
          maintenanceActors.get(id) match {
            case Some(ref) =>
              ref ! Command.Get(id, replyTo)
            case None      =>
              replyTo ! CommandResponse.Failure("Maintenance not found")
          }
        )

    case Command.GetAll(replyTo: ActorRef[CommandResponse[Map[UUID, Maintenance]]]) =>
      val maintenanceActors = state.state.getOrElse(ManagerState(Map.empty)).actors

      Effect.none
        .thenRun { _ =>
          import context.executionContext

          import scala.concurrent.Future
          import scala.concurrent.duration._
          implicit val timeout: Timeout = 3.seconds

          if (maintenanceActors.isEmpty) {
            replyTo ! CommandResponse.Success(Some(Map.empty))
          } else {
            val futures = maintenanceActors.map { case (id, actor) =>
              actor
                .ask[CommandResponse[Maintenance]](ref => Command.Get(id, ref))(timeout, context.system.scheduler)
                .map(response => id -> response)
            }

            Future.sequence(futures).foreach { responses =>
              val result = responses.collect { case (id, CommandResponse.Success(Some(maintenance))) =>
                id -> maintenance
              }.toMap

              replyTo ! CommandResponse.Success(Some(result))
            }
          }
        }

    case Command.Schedule(id, replyTo) =>
      val maintenanceActors = state.state.getOrElse(ManagerState(Map.empty)).actors
      Effect.none
        .thenRun(_ =>
          maintenanceActors.get(id) match {
            case Some(ref) =>
              ref ! Command.Schedule(id, replyTo)
            case None      =>
              replyTo ! CommandResponse.Failure("Maintenance not found")
          }
        )

    case createMaintenance @ Command.Create(maintenance, _) =>
      val ref = context.spawn(MaintenanceActor(maintenance.id), s"maintenance-${maintenance.id}")

      Effect
        .persist(Created(maintenance))
        .thenReply(ref)(_ => createMaintenance)
  }

  private def handleEvent(
    context: ActorContext[Command]
  )(state: State[ManagerState], event: Event): State[ManagerState] = event match {
    case Created(maintenance) =>
      val maintenanceActorId  = s"maintenance-${maintenance.id}"
      val maintenanceActorRef = context
        .child(maintenanceActorId) // exists after command handler,
        .getOrElse(context.spawn(MaintenanceActor(maintenance.id), maintenanceActorId))
        .unsafeUpcast[AnyRef]
        .narrow[Command]
      val maintenanceActors   = state.state.getOrElse(ManagerState(Map.empty)).actors
      State(Some(ManagerState(maintenanceActors + (maintenance.id -> maintenanceActorRef))))
  }
}
