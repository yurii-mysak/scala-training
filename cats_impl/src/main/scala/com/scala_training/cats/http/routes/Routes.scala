package com.scala_training.cats.http.routes

import cats.effect.Concurrent
import com.scala_training.cats.application.{CarService, MaintenanceService}
import com.scala_training.cats.http.handlers.{CarResponseHandler, HttpErrorHandler, MaintenanceResponseHandler}
import com.scala_training.cats.persistence.repository.{CarRepositoryAPI, MaintenanceRepositoryAPI}
import com.scala_training.cats.http.handlers.*
import org.http4s.HttpRoutes
import org.typelevel.log4cats.{Logger, LoggerFactory}

object Routes {

  def carRoutes[F[_]: {Concurrent, LoggerFactory}](repo: CarRepositoryAPI[F]): HttpRoutes[F] = {
    given logger: Logger[F] = LoggerFactory[F].getLogger

    val service         = new CarService[F](repo)
    val errorHandler    = new HttpErrorHandler[F]
    val responseHandler = new CarResponseHandler[F]
    val routes          = new CarRoutes[F](service, errorHandler, responseHandler)
    routes.routes
  }

  def maintenanceRoutes[F[_]: {Concurrent, LoggerFactory}](repo: MaintenanceRepositoryAPI[F]): HttpRoutes[F] = {
    given logger: Logger[F] = LoggerFactory[F].getLogger

    val service         = new MaintenanceService[F](repo)
    val errorHandler    = new HttpErrorHandler[F]
    val responseHandler = new MaintenanceResponseHandler[F]
    val routes          = new MaintenanceRoutes[F](service, errorHandler, responseHandler)
    routes.routes
  }
}
