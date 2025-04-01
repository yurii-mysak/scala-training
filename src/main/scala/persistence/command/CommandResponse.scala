package persistence.command

import scala.reflect.ClassTag

// Responses
sealed trait CommandResponse[Model]

object CommandResponse {
  case class Success[Model: ClassTag](model: Option[Model]) extends CommandResponse[Model]
  case class Failure[Model: ClassTag](reason: String) extends CommandResponse[Model]
}