package com.scala_training.core.domain.adt

import akka.serialization.jackson.JsonSerializable
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import doobie.Meta

@JsonSerialize(`using` = classOf[MaintenanceStatusJsonSerializer])
@JsonDeserialize(`using` = classOf[MaintenanceStatusJsonDeserializer])
sealed trait MaintenanceStatus extends JsonSerializable {
  def value: String
}

object MaintenanceStatus {

  given maintenanceStatusMeta: Meta[MaintenanceStatus] = Meta[String].timap[MaintenanceStatus](str =>
    MaintenanceStatus
      .fromString(str)
  )(_.value)

  case object Created extends MaintenanceStatus {
    override val value: String = "created"
  }

  case object Scheduled extends MaintenanceStatus {
    override val value: String = "scheduled"
  }

  case object Pending extends MaintenanceStatus {
    override val value: String = "pending"
  }

  case object InProgress extends MaintenanceStatus {
    override val value: String = "in_progress"
  }

  case object Completed extends MaintenanceStatus {
    override val value: String = "completed"
  }

  case object Cancelled extends MaintenanceStatus {
    override val value: String = "cancelled"
  }

  private val values: Set[MaintenanceStatus] = Set(
    Created,
    Scheduled,
    Pending,
    InProgress,
    Completed,
    Cancelled
  )

  def fromString(str: String): MaintenanceStatus = values
    .find(_.value == str)
    .getOrElse(
      throw new IllegalArgumentException(s"Invalid maintenance status: $str")
    )
}

class MaintenanceStatusJsonSerializer extends StdSerializer[MaintenanceStatus](classOf[MaintenanceStatus]) {

  override def serialize(value: MaintenanceStatus, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeString(value.value)
}

class MaintenanceStatusJsonDeserializer extends StdDeserializer[MaintenanceStatus](classOf[MaintenanceStatus]) {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): MaintenanceStatus = {
    if (p.getCurrentToken == JsonToken.VALUE_NULL) {
      throw new IllegalArgumentException("Null value is not allowed")
    }

    val text = p.getText
    if (text == null || text.isEmpty) {
      throw new IllegalArgumentException("Empty value is not allowed")
    }

    MaintenanceStatus.fromString(text)
  }

  override def getNullValue(ctxt: DeserializationContext): MaintenanceStatus =
    throw new IllegalArgumentException("Null value is not allowed")
}
