package com.github.scala_training
package persistence.model.car

import persistence.command.car.Command
import persistence.event.car.Event

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import com.github.scala_training.persistence.command.CommandResponse
import com.github.scala_training.persistence.model.State


import spray.json.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import com.github.scala_training.http.mapping.CarJsonProtocol.*

import java.time.LocalDateTime
import java.util.UUID

final case class Car(
                      id: UUID,
                      make: String,
                      model: String,
                      year: Int,
                      createdAt: LocalDateTime,
                      updatedAt: Option[LocalDateTime] = None
                    )

object Car {

  def apply(id: UUID): Behavior[Command] = {
    Behaviors.setup { context =>
      EventSourcedBehavior(
        persistenceId = PersistenceId.ofUniqueId(s"car-$id"),
        emptyState = State(None),
        commandHandler = (state, command) => handleCommand(state, command, context),
        eventHandler = (state, event) => handleEvent(state, event)
      )
    }
  }

  private def handleCommand(
                             state: State[Car],
                             command: Command,
                             context: akka.actor.typed.scaladsl.ActorContext[Command]
                           ): akka.persistence.typed.scaladsl.Effect[Event, State[Car]] = {
    import Command.*

    command match {
      case Create(make, model, year, replyTo) =>
        if (state.state.isDefined)
          akka.persistence.typed.scaladsl.Effect.none
            .thenRun(_ => replyTo ! CommandResponse.Failure("Car already exists"))
        else {
          val car = Car(
            id = UUID.randomUUID(),
            make = make,
            model = model,
            year = year,
            createdAt = LocalDateTime.now()
          )
          akka.persistence.typed.scaladsl.Effect
            .persist(Event.Created(car))
            .thenRun(_ => replyTo ! CommandResponse.Success(Some(car)))
        }

      // Implement other command handlers here
      case _ => akka.persistence.typed.scaladsl.Effect.none
    }
  }

  private def handleEvent(state: State[Car], event: Event): State[Car] = {
    import Event.*

    event match {
      case Created(car) => State(Some(car))
      case Updated(car) => State(Some(car))
      case Deleted(_) => State(None)
    }
  }
}