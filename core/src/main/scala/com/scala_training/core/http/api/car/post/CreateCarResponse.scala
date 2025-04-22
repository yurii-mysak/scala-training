package com.scala_training.core.http.api.car.post

import com.scala_training.core.domain.model.CarCore
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID

case class CreateCarResponse[A <: CarCore](id: UUID)

object CreateCarResponse {

  given [A <: CarCore]: Conversion[CommandResponse[A], CreateCarResponse[A]] with {

    def apply(response: CommandResponse[A]): CreateCarResponse[A] = response match {
      case CommandResponse.Success(Some(car: CarCore)) =>
        CreateCarResponse(car.id)
      case CommandResponse.Success(None)               =>
        throw new IllegalStateException("Car creation succeeded but no car was returned")
      case CommandResponse.Failure(reason)             =>
        throw new IllegalStateException(reason)
    }
  }
}
