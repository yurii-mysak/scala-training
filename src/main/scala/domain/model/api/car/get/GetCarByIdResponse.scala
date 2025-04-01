package domain.model.api.car.get

import persistence.command.CommandResponse
import persistence.model.car.Car

import java.time.Year
import java.util.UUID

case class GetCarByIdResponse(id: UUID, make: String, model: String, year: Year)

object GetCarByIdResponse {
  implicit def toGetCarByIdResponse(response: CommandResponse[Car]): GetCarByIdResponse = {
    response match {
      case CommandResponse.Success(Some(car)) =>
        GetCarByIdResponse(car.id, car.make, car.model, car.year)
      case CommandResponse.Success(None) =>
        throw new IllegalStateException(s"Car was not found")
      case CommandResponse.Failure(reason) =>
        throw new IllegalStateException(reason)
    }
  }
}
