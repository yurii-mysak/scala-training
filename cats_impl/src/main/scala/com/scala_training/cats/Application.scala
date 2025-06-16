package com.scala_training.cats

import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.implicits.*
import com.comcast.ip4s.*
import com.scala_training.cats.config.Loader
import com.scala_training.cats.http.routes.{Routes, StreamingRoutes}
import com.scala_training.cats.persistence.db.FlywayMigrator
import com.scala_training.cats.persistence.repository.{CarRepository, MaintenanceRepository}
import com.scala_training.kafka.client.KafkaCatsClient
import doobie.hikari.HikariTransactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory

import scala.concurrent.duration.*
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Application extends IOApp {
  private val parallelism = 5

  // Dedicated EC for blocking operations (DB)
  private val blockingEC = ExecutionContext.fromExecutor(
    Executors.newWorkStealingPool(parallelism)
  )

  given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  given logger: Logger[IO]               = loggerFactory.getLogger

  private def makeServer[F[_]: Async](
    using lf: LoggerFactory[F]
  ) = for {
    config                                   <- Resource.eval(Loader.load[F])
    databaseConfig                            = config.database.getOrElse(
                                                  throw new IllegalArgumentException("Database configuration is missing")
                                                )
    _                                        <- Resource.eval(FlywayMigrator.execute[F](databaseConfig))
    xa                                       <- HikariTransactor.newHikariTransactor[F](
                                                  databaseConfig.driver,
                                                  databaseConfig.url,
                                                  databaseConfig.user,
                                                  databaseConfig.password,
                                                  blockingEC // keep this for DB operations
                                                )
    kafkaClient                               = new KafkaCatsClient[F](
                                                  bootstrap = config.kafka.bootstrapServers
                                                )
    carRepo: CarRepository[F]                 = new CarRepository[F](xa, kafkaClient, config.kafka.carEventsTopic)
    maintenanceRepo: MaintenanceRepository[F] =
      new MaintenanceRepository[F](xa, kafkaClient, config.kafka.maintenanceEventsTopic)
    carRoutes                                 = Routes.carRoutes[F](carRepo)
    maintenanceRoutes                         = Routes.maintenanceRoutes[F](maintenanceRepo)
    streamingRoutes                           = StreamingRoutes[F](carRepo)
    httpAppRoutes                             = carRoutes <+> maintenanceRoutes <+> streamingRoutes
    httpApp                                  <- Resource.eval(CORS.policy.withAllowOriginAll.apply(httpAppRoutes.orNotFound))
    server                                   <- EmberServerBuilder
                                                  .default[F]
                                                  .withHost(Host.fromString(config.server.host).getOrElse(Host.fromString("localhost").get))
                                                  .withPort(Port.fromInt(config.server.port).getOrElse(Port.fromInt(8080).get))
                                                  .withIdleTimeout(config.server.timeout.getOrElse(10.seconds))
                                                  .withHttpApp(httpApp)
                                                  .build
  } yield server

  override def run(args: List[String]): IO[ExitCode] = {
    val port = 8080
    val host = org.http4s.server.defaults.IPv4Host

    makeServer[IO]
      .use(_ =>
        logger.info(s"Server ready at $host:$port") *>
          IO.never
      )
      .as(ExitCode.Success)
  }
}
