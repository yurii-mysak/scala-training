package http.api.car.get

import domain.model.CarCore
import persistence.command.CommandResponse

import java.time.Year
import java.util.UUID

case class GetCarByIdResponse[A <: CarCore](id: UUID, make: String, model: String, year: Year)

object GetCarByIdResponse {

  implicit def toGetCarByIdResponse[A <: CarCore](response: CommandResponse[A]): GetCarByIdResponse[A] =
    response match {
      case CommandResponse.Success(Some(car: A)) =>
        GetCarByIdResponse(car.id, car.make, car.model, car.year)
      case CommandResponse.Success(None)         =>
        throw new IllegalStateException(s"Car was not found")
      case CommandResponse.Failure(reason)       =>
        throw new IllegalStateException(reason)
    }
}
