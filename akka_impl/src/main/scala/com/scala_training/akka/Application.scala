package com.scala_training.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.util.Timeout
import http.controller.{Car, Maintenance}
import persistence.model.car.CarManager
import persistence.model.maintenance.MaintenanceManager
import persistence.repository.{CarRepository, MaintenanceRepository}

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Application {

  def main(args: Array[String]): Unit = {
    // Create Actor system
    given system: ActorSystem[?]       = ActorSystem(Behaviors.empty, "CarMine")
    given ec: ExecutionContextExecutor =
      system.dispatchers.lookup(DispatcherSelector.fromConfig("akka.dispatchers.http-dispatcher"))
    given timeout: Timeout             = 3.seconds

    val carManager            = system.systemActorOf(CarManager(), "car-manager")
    val maintenanceManager    = system.systemActorOf(MaintenanceManager(), "maintenance-manager")
    val carRepository         = new CarRepository(carManager)
    val maintenanceRepository = new MaintenanceRepository(maintenanceManager)

    // Combine routes
    val routes = concat(
      pathPrefix("api") {
        concat(
          Car.routes(carRepository),
          Maintenance.routes(maintenanceRepository)
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
      case Failure(ex)      =>
        system.log.error(s"Failed to bind HTTP server: ${ex.getMessage}")
        system.terminate()
    }
  }
}
