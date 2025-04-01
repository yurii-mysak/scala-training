package domain.model.api.car.post

import persistence.command.CommandResponse
import persistence.model.car.Car

import java.util.UUID

case class CreateCarResponse(id: UUID)

object CreateCarResponse {
  implicit def toCreateCarResponse(response: CommandResponse[Car]): CreateCarResponse = {
    response match {
      case CommandResponse.Success(Some(car)) =>
        CreateCarResponse(car.id)
      case CommandResponse.Success(None) =>
        throw new IllegalStateException("Car creation succeeded but no car was returned")
      case CommandResponse.Failure(reason) =>
        throw new IllegalStateException(reason)
    }
  }
}
