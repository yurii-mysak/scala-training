package com.scala_training.kafka.model

import com.scala_training.core.domain.model.CarCore
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

sealed trait CarEvent extends KafkaEvent {
  def car: CarCore
}

object CarEvent {

  case class CarCreated[A <: CarCore](
    car: A
  ) extends CarEvent

  given [A <: CarCore: {Encoder, Decoder}]: Encoder[CarCreated[A]] = deriveEncoder

  given [A <: CarCore: {Encoder, Decoder}]: Decoder[CarCreated[A]] = deriveDecoder
}
