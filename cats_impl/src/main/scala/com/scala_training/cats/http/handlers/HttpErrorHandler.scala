package com.scala_training.cats.http.handlers

import cats.effect.Sync
import com.scala_training.core.domain.adt.AppError
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import cats.effect._
import cats.syntax.all._

class HttpErrorHandler[F[_]: Sync: Logger] extends Http4sDsl[F] {
  import AppError._

  def handleError(message: String)(error: Throwable): F[Response[F]] = for {
    _        <- Logger[F].error(s"Error occurred: $message: ${error.getMessage}")
    response <- error match {
                  case e: AppError.NotFound      =>
                    NotFound((e: AppError).asJson)
                  case e: AppError.BadRequest    =>
                    BadRequest((e: AppError).asJson)
                  case e: AppError.InternalError =>
                    InternalServerError((e: AppError).asJson)
                  case e: Throwable              =>
                    InternalServerError(
                      (AppError.InternalError(e.getMessage): AppError).asJson
                    )
                }
  } yield response
}
