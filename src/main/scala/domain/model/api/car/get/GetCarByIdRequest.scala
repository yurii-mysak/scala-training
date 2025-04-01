package domain.model.api.car.get

import java.util.UUID

case class GetCarByIdRequest(id: UUID)

object GetCarByIdRequest {
  implicit def toGetCarByIdRequest(path: String): GetCarByIdRequest = {
    try {
      val uuid = UUID.fromString(path)
      GetCarByIdRequest(uuid)
    } catch {
      case _: IllegalArgumentException =>
        throw new IllegalArgumentException(s"Invalid UUID format: $path")
    }
  }
}