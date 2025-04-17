package com.scala_training.cats.http.handlers

import cats.effect.Sync
import cats.implicits._
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

class HttpErrorHandler[F[_]: Sync: Logger] extends Http4sDsl[F] {

  def handleError(message: String)(error: Throwable): F[Response[F]] =
    Logger[F].error(s"$message: ${error.getMessage}") *> InternalServerError()
}
