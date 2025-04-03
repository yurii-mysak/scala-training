package domain.model.api.car.get

import persistence.command.CommandResponse
import persistence.model.car.Car

import java.util.UUID

case class GetCarsResponse(cars: Map[UUID, GetCarByIdResponse])

object GetCarsResponse {
  implicit def toGetCarsResponse(response: CommandResponse[Map[UUID, Car]]): GetCarsResponse = {
    response match {
      case CommandResponse.Success(Some(cars: Map[UUID, Car])) =>
        GetCarsResponse(cars.map {
          case (id, car) => id -> GetCarByIdResponse(car.id, car.make, car.model, car.year)
        })
      case CommandResponse.Failure(reason) =>
        throw new IllegalStateException(reason)
    }
  }
}
