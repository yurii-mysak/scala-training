package com.scala_training.cats

import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.implicits.*
import com.comcast.ip4s.*
import com.scala_training.cats.config.Loader
import com.scala_training.cats.http.routes.Routes
import com.scala_training.cats.persistence.db.FlywayMigrator
import com.scala_training.cats.persistence.repository.{CarRepository, MaintenanceRepository}
import doobie.hikari.HikariTransactor
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.LoggerFactory

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Application extends IOApp {
  private val httpPoolSize = 32
  private val parallelism  = 5

  // Dedicated EC for HTTP operations
  private val httpEC: ExecutionContextExecutor = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(httpPoolSize)
  )

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
    _                                        <- Resource.eval(FlywayMigrator.execute[F](config.database))
    xa                                       <- HikariTransactor.newHikariTransactor[F](
                                                  config.database.driver,
                                                  config.database.url,
                                                  config.database.user,
                                                  config.database.password,
                                                  blockingEC // keep this for DB operations
                                                )
    carRepo: CarRepository[F]                 = new CarRepository[F](xa)
    maintenanceRepo: MaintenanceRepository[F] = new MaintenanceRepository[F](xa)
    carRoutes                                 = Routes.carRoutes[F](carRepo)
    maintenanceRoutes                         = Routes.maintenanceRoutes[F](maintenanceRepo)
    httpAppRoutes                             = carRoutes <+> maintenanceRoutes
    // NOTE: really crazy httpapp definition with cors, not sure how to work around?
    httpApp                                  <- Resource.eval(CORS.policy.withAllowOriginAll.apply(httpAppRoutes.orNotFound))
    server                                   <- EmberServerBuilder
                                                  .default[F]
                                                  .withHost(Host.fromString(config.server.host).getOrElse(Host.fromString("localhost").get))
                                                  .withPort(Port.fromInt(config.server.port).getOrElse(Port.fromInt(8080).get))
                                                  .withIdleTimeout(config.server.timeout)
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
