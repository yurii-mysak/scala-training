package com.scala_training.core.http.api.car.get

import com.scala_training.core.domain.model.CarCore
import com.scala_training.core.persistence.command.CommandResponse

import java.util.UUID

case class GetCarsResponse[A <: CarCore](cars: Map[UUID, GetCarByIdResponse[A]])

object GetCarsResponse {

  implicit def toGetCarsResponse[A <: CarCore](response: CommandResponse[Map[UUID, A]]): GetCarsResponse[A] =
    response match {
      case CommandResponse.Success(Some(cars: Map[UUID, CarCore])) =>
        GetCarsResponse(cars.map { case (id, car) =>
          id -> GetCarByIdResponse[A](car.id, car.make, car.model, car.year)
        })
      case CommandResponse.Success(None)                           =>
        throw new IllegalStateException("No cars found")
      case CommandResponse.Failure(reason)                         =>
        throw new IllegalStateException(reason)
    }
}
