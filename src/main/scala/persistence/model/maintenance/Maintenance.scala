package persistence.model.maintenance

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import domain.adt.{MaintenanceStatus, MaintenanceType}
import persistence.command.CommandResponse
import persistence.command.maintenance.Command
import persistence.event.maintenance.Event
import persistence.model.State

import java.time.LocalDateTime
import java.util.UUID

case class Maintenance(
                              id: UUID,
                              carId: UUID,
                              description: String,
                              maintenanceType: MaintenanceType,
                              scheduledDate: LocalDateTime,
                              status: MaintenanceStatus,
                              createdAt: LocalDateTime,
                              updatedAt: Option[LocalDateTime] = None
                            )

object Maintenance {

  def apply(id: UUID): Behavior[Command] = {
    Behaviors.setup { context =>
      EventSourcedBehavior(
        persistenceId = PersistenceId.ofUniqueId(s"maintenance-$id"),
        emptyState = State(None),
        commandHandler = (state, command) => handleCommand(state, command, context),
        eventHandler = (state, event) => handleEvent(state, event)
      )
    }
  }

  private def handleCommand(
                             state: State[Maintenance],
                             command: Command,
                             context: akka.actor.typed.scaladsl.ActorContext[Command]
                           ): akka.persistence.typed.scaladsl.Effect[Event, State[Maintenance]] = {
    import Command._

    command match {
      case Schedule(carId, description, maintenanceType, scheduledDate, replyTo) =>
        if (state.state.isDefined)
          akka.persistence.typed.scaladsl.Effect.none
            .thenRun(_ => replyTo ! CommandResponse.Failure("Maintenance already exists"))
        else {
          val maintenance = Maintenance(
            id = UUID.randomUUID(),
            carId = carId,
            description = description,
            maintenanceType = maintenanceType,
            scheduledDate = scheduledDate,
            status = MaintenanceStatus.Scheduled,
            createdAt = LocalDateTime.now()
          )
          akka.persistence.typed.scaladsl.Effect
            .persist(Event.Scheduled(maintenance))
            .thenRun(_ => replyTo ! CommandResponse.Success(Some(maintenance)))
        }

      // Implement other command handlers here
      case _ => akka.persistence.typed.scaladsl.Effect.none
    }
  }

  private def handleEvent(state: State[Maintenance], event: Event): State[Maintenance] = {
    import Event._

    event match {
      case Scheduled(maintenance) => State(Some(maintenance))
      case StatusUpdated(maintenance) => State(Some(maintenance))
      case Rescheduled(maintenance) => State(Some(maintenance))
      case Cancelled(_) => State(None)
    }
  }
}