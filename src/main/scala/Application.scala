import akka.actor.typed.{ActorSystem, Behavior, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import http.controller.{Car, Maintenance}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import persistence.model.car.CarManager

import scala.concurrent.duration._
import persistence.repository.CarRepository

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Application {
  def main(args: Array[String]): Unit = {
    // Create Actor system
    implicit val system: ActorSystem[?] = ActorSystem(Behaviors.empty, "CarMine")
    implicit val ec: ExecutionContextExecutor = system.dispatchers.lookup(DispatcherSelector.fromConfig("akka.dispatchers.http-dispatcher"))
    implicit val timeout: Timeout = 3.seconds
    // todo: somehow should be a behaviour and passed differently?
    val carManager = system.systemActorOf(CarManager(), "car-manager")

    val carRepository = new CarRepository(carManager)

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
