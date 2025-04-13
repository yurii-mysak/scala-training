package http.routes

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger
import persistence.repository.{CarRepositoryAPI, MaintenanceRepositoryAPI}
import application.{CarService, MaintenanceService}
import http.handlers._

object Routes {

  def carRoutes[F[_]: Sync: Logger](repo: CarRepositoryAPI[F]): F[HttpRoutes[F]] = {
    val service         = new CarService[F](repo)
    val errorHandler    = new HttpErrorHandler[F]
    val responseHandler = new CarResponseHandler[F]
    new CarRoutes[F](service, errorHandler, responseHandler).routes.pure[F]
  }

  def maintenanceRoutes[F[_]: Sync: Logger](repo: MaintenanceRepositoryAPI[F]): F[HttpRoutes[F]] = {
    val service         = new MaintenanceService[F](repo)
    val errorHandler    = new HttpErrorHandler[F]
    val responseHandler = new MaintenanceResponseHandler[F]
    new MaintenanceRoutes[F](service, errorHandler, responseHandler).routes.pure[F]
  }
}
