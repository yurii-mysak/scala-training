package com.scala_training.akka.persistence.model.car

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import cats.effect.IO
import com.scala_training.akka.persistence.command.car.Command
import com.scala_training.akka.persistence.event.car.Event
import com.scala_training.akka.persistence.event.car.Event.*
import com.scala_training.akka.persistence.model.State
import com.scala_training.core.persistence.command.CommandResponse
import com.scala_training.kafka.client.KafkaClient
import com.scala_training.kafka.model.CarEvent

import java.util.UUID

object CarManager {
  private case class ManagerState(actors: Map[UUID, ActorRef[Command]])

  def apply(kafkaClient: KafkaClient[IO], topic: String): Behavior[Command] = Behaviors.setup { context =>
    EventSourcedBehavior[Command, Event, State[ManagerState]](
      persistenceId = PersistenceId.ofUniqueId("car-manager"),
      emptyState = State(Some(ManagerState(Map.empty))),
      commandHandler = handleCommand(context, kafkaClient, topic),
      eventHandler = handleEvent(context)
    )
  }

  private def handleCommand(
    context: ActorContext[Command],
    kafkaClient: KafkaClient[IO],
    topic: String
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
          import scala.concurrent.duration.*
          given timeout: Timeout = 3.seconds

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

    case createCar @ Command.Create(car, replyTo) =>
      val ref = context.spawn(Car(car.id), s"car-${car.id}")

      Effect
        .persist(Created(car))
        .thenRun { _ =>
          replyTo ! CommandResponse.Success(Some(car))

          kafkaClient.produce(topic, CarEvent.CarCreated(car)).compile.drain
        }
  }

  private def handleEvent(
    context: ActorContext[Command]
  )(state: State[ManagerState], event: Event): State[ManagerState] = event match {
    case Created(car) =>
      val carActorId  = s"car-${car.id}"
      val carActorRef = context
        .child(carActorId) // exists after command handler,
        .getOrElse(context.spawn(Car(car.id), carActorId))
        .unsafeUpcast[AnyRef]
        .narrow[Command]
      val carActors   = state.state.getOrElse(ManagerState(Map.empty)).actors
      State(Some(ManagerState(carActors + (car.id -> carActorRef))))
  }
}
