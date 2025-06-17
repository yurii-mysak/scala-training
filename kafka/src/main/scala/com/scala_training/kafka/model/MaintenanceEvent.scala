package com.scala_training.kafka.model

import com.scala_training.core.domain.model.MaintenanceCore
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait MaintenanceEvent extends KafkaEvent {
  def maintenance: MaintenanceCore
}

object MaintenanceEvent {

  case class MaintenanceCreated[A <: MaintenanceCore](
    maintenance: A
  ) extends MaintenanceEvent

  case class MaintenanceUpdated[A <: MaintenanceCore](
    maintenance: A
  ) extends MaintenanceEvent

  given [A <: MaintenanceCore: {Encoder, Decoder}]: Encoder[MaintenanceCreated[A]] = deriveEncoder

  given [A <: MaintenanceCore: {Encoder, Decoder}]: Decoder[MaintenanceCreated[A]] = deriveDecoder

  given [A <: MaintenanceCore: {Encoder, Decoder}]: Encoder[MaintenanceUpdated[A]] = deriveEncoder

  given [A <: MaintenanceCore: {Encoder, Decoder}]: Decoder[MaintenanceUpdated[A]] = deriveDecoder
}
