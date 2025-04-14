package http.api.car.post

import domain.model.CarCore
import persistence.command.CommandResponse

import java.util.UUID

case class CreateCarResponse[A <: CarCore](id: UUID)

object CreateCarResponse {

  implicit def toCreateCarResponse[A <: CarCore](response: CommandResponse[A]): CreateCarResponse[A] = response match {
    case CommandResponse.Success(Some(car: CarCore)) =>
      CreateCarResponse(car.id)
    case CommandResponse.Success(None)               =>
      throw new IllegalStateException("Car creation succeeded but no car was returned")
    case CommandResponse.Failure(reason)             =>
      throw new IllegalStateException(reason)
  }
}
