package com.scala_training.cats.http.routes

import cats.effect.Sync
import com.scala_training.cats.application.{CarService, MaintenanceService}
import com.scala_training.cats.http.handlers.{CarResponseHandler, HttpErrorHandler, MaintenanceResponseHandler}
import com.scala_training.cats.persistence.repository.{CarRepositoryAPI, MaintenanceRepositoryAPI}
import com.scala_training.cats.http.handlers._
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger

object Routes {

  def carRoutes[F[_]: Sync: Logger](repo: CarRepositoryAPI[F]): F[HttpRoutes[F]] = {
    val service         = new CarService[F](repo)
    val errorHandler    = new HttpErrorHandler[F]
    val responseHandler = new CarResponseHandler[F]
    val routes          = new CarRoutes[F](service, errorHandler, responseHandler)
    Sync[F].pure(routes.routes)
  }

  def maintenanceRoutes[F[_]: Sync: Logger](repo: MaintenanceRepositoryAPI[F]): F[HttpRoutes[F]] = {
    val service         = new MaintenanceService[F](repo)
    val errorHandler    = new HttpErrorHandler[F]
    val responseHandler = new MaintenanceResponseHandler[F]
    val routes          = new MaintenanceRoutes[F](service, errorHandler, responseHandler)
    Sync[F].pure(routes.routes)
  }
}
