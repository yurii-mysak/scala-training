package http.api.car.get

import domain.model.CarCore
import persistence.command.CommandResponse

import java.util.UUID

case class GetCarsResponse[A <: CarCore](cars: Map[UUID, GetCarByIdResponse[A]])

object GetCarsResponse {

  implicit def toGetCarsResponse[A <: CarCore](response: CommandResponse[Map[UUID, A]]): GetCarsResponse[A] =
    response match {
      case CommandResponse.Success(Some(cars: Map[UUID, CarCore])) =>
        GetCarsResponse(cars.map { case (id, car) =>
          id -> GetCarByIdResponse[A](car.id, car.make, car.model, car.year)
        })
      case CommandResponse.Failure(reason)                         =>
        throw new IllegalStateException(reason)
    }
}
