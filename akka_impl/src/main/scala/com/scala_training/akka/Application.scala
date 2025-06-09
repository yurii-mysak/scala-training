package com.scala_training.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.util.Timeout
import cats.effect.{IO, IOApp, Resource}
import com.scala_training.akka.config.Loader
import com.scala_training.kafka.client.KafkaClient
import http.controller.{Car, Maintenance}
import org.slf4j.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import persistence.model.car.CarManager
import persistence.model.maintenance.MaintenanceManager
import persistence.repository.{CarRepository, MaintenanceRepository}

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

object Application extends IOApp.Simple {

  override def run: IO[Unit] = {
    val program = for {
      system <- IO(ActorSystem(Behaviors.empty, "CarMine")).toResource

      given ActorSystem[?]                           = system
      given ExecutionContextExecutor                 =
        system.dispatchers.lookup(DispatcherSelector.fromConfig("akka.dispatchers.http-dispatcher"))
      given Timeout                                  = Timeout(3.seconds)
      given org.slf4j.Logger                         = LoggerFactory.getLogger(getClass)
      given org.typelevel.log4cats.LoggerFactory[IO] = Slf4jFactory.create[IO]

      config                = Loader.load()
      kafkaClient           = new KafkaClient[IO](
                                bootstrap = config.kafka.bootstrapServers
                              )
      carManager            = system.systemActorOf(CarManager(kafkaClient, config.kafka.carEventsTopic), "car-manager")
      maintenanceManager    = system.systemActorOf(MaintenanceManager(), "maintenance-manager")
      carRepository         = new CarRepository(carManager)
      maintenanceRepository = new MaintenanceRepository(maintenanceManager)

      routes = concat(
                 pathPrefix("api") {
                   concat(
                     Car.routes(carRepository),
                     Maintenance.routes(maintenanceRepository)
                   )
                 }
               )

      binding <- Resource.make(
                   IO.fromFuture(
                     IO(
                       Http().newServerAt(config.server.host, config.server.port).bind(routes)
                     )
                   )
                 )(binding => IO.fromFuture(IO(binding.terminate(10.seconds))).void)

      _ <- Resource.eval(
             IO.delay {
               val address = binding.localAddress
               summon[org.slf4j.Logger].info(s"Server online at http://${address.getHostString}:${address.getPort}/")
             }
           )
    } yield ()

    program.use(_ => IO.never)
  }
}
