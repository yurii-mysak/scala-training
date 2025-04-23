package com.scala_training.core.domain.adt

import io.circe.{Encoder, Json}

sealed trait AppError extends Throwable {
  def message: String
  def code: String

  override def getMessage: String = message
}

object AppError {

  case class NotFound(message: String) extends AppError {
    val code = "NOT_FOUND"
  }

  case class BadRequest(message: String) extends AppError {
    val code = "BAD_REQUEST"
  }

  case class InternalError(message: String) extends AppError {
    val code = "INTERNAL_ERROR"
  }

  given encoder: Encoder[AppError] = (error: AppError) =>
    Json.obj(
      "code"    -> Json.fromString(error.code),
      "message" -> Json.fromString(error.message)
    )
}
