package com.github.scala_training

import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import http.controller.{Car, Maintenance}
import com.github.scala_training.persistence.model.car.{Car => CarModel}

import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.duration.*
import com.github.scala_training.persistence.command.car.Command
import com.github.scala_training.persistence.repository.CarRepository

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Application {
  @main def app(args: String*): Unit = {
    // Create Actor system
    given system: ActorSystem[?] = ActorSystem(Behaviors.empty, "CarMine")
    given ExecutionContextExecutor = system.dispatchers.lookup(DispatcherSelector.fromConfig("akka.dispatchers.http-dispatcher"))
    given timeout: Timeout = 3.seconds

    // Create CarRepository instance
    val carActor: ActorRef[Command] = system.systemActorOf(CarModel(UUID.randomUUID()), "carActor")
    val carRepository = new CarRepository(carActor)

    // Combine routes
    val routes = concat(
      pathPrefix("api") {
        concat(
          Car.routes(carRepository),
          Maintenance.routes
        )
      }
    )

    // Start the server
    val serverBinding: Future[Http.ServerBinding] =
      Http().newServerAt("localhost", 8080).bind(routes)

    // Handle server binding result
    serverBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        system.log.error(s"Failed to bind HTTP server: ${ex.getMessage}")
        system.terminate()
    }
  }
}
