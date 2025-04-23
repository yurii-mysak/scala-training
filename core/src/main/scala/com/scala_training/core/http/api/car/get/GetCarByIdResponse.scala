package com.scala_training.core.http.api.car.get

import com.scala_training.core.domain.model.CarCore
import com.scala_training.core.persistence.command.CommandResponse

import java.time.Year
import java.util.UUID

case class GetCarByIdResponse[A <: CarCore](id: UUID, make: String, model: String, year: Year)

object GetCarByIdResponse {

  given [A <: CarCore]: Conversion[CommandResponse[A], GetCarByIdResponse[A]] with {

    def apply(response: CommandResponse[A]): GetCarByIdResponse[A] = response match {
      case CommandResponse.Success(Some(car: A)) =>
        GetCarByIdResponse(car.id, car.make, car.model, car.year)
      case CommandResponse.Success(None)         =>
        throw new IllegalStateException(s"Car was not found")
      case CommandResponse.Failure(reason)       =>
        throw new IllegalStateException(reason)
    }
  }
}
