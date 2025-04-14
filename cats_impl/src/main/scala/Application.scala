package com.training.cats_impl

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits.toSemigroupKOps
import config.Loader
import doobie.hikari.HikariTransactor
import http.routes.Routes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import persistence.db.FlywayMigrator
import persistence.repository.{CarRepository, MaintenanceRepository}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Application extends IOApp {
  private val httpPoolSize                   = 32
  private val parallelism                    = 5
  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  // Dedicated EC for HTTP operations
  private val httpEC: ExecutionContextExecutor = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(httpPoolSize)
  )

  implicit override def contextShift: ContextShift[IO] = IO.contextShift(httpEC)

  // Dedicated EC for blocking operations (DB)
  private val blockingEC = ExecutionContext.fromExecutor(
    Executors.newWorkStealingPool(parallelism)
  )

  private val blockerResource: Resource[IO, Blocker] =
    Blocker[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    val port = 8080
    val host = org.http4s.server.defaults.IPv4Host

    val makeServer = for {
      config            <- Resource.eval(Loader.load[IO])
      _                 <- Resource.eval(FlywayMigrator.execute[IO](config.database))
      blocker           <- blockerResource
      xa                <- HikariTransactor.newHikariTransactor[IO](
                             config.database.driver,
                             config.database.url,
                             config.database.user,
                             config.database.password,
                             blockingEC,
                             blocker
                           )
      carRepo            = new CarRepository[IO](xa)
      maintenanceRepo    = new MaintenanceRepository[IO](xa)
      carRoutes         <- Resource.eval(Routes.carRoutes[IO](carRepo))
      maintenanceRoutes <- Resource.eval(Routes.maintenanceRoutes[IO](maintenanceRepo))
      httpApp            = CORS((carRoutes <+> maintenanceRoutes).orNotFound)
      server            <- EmberServerBuilder
                             .default[IO]
                             .withHost(config.server.host)
                             .withPort(config.server.port)
                             .withIdleTimeout(config.server.timeout)
                             .withHttpApp(httpApp)
                             .build
    } yield server

    makeServer.use(_ => IO(Console.println(s"Server ready at $host:$port")) *> IO.never).as(ExitCode.Success)
  }
}
