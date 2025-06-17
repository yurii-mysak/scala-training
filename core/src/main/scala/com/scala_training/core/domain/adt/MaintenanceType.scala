package com.scala_training.core.domain.adt

import akka.serialization.jackson.JsonSerializable
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.util.UUID

@JsonSerialize(`using` = classOf[MaintenanceTypeJsonSerializer])
@JsonDeserialize(`using` = classOf[MaintenanceTypeJsonDeserializer])
sealed trait MaintenanceType extends JsonSerializable {
  def id: UUID
  def value: String
}

object MaintenanceType {

  case object OilChange extends MaintenanceType {
    override val id: UUID      = UUID.fromString("10A6A1EC-0794-4778-AE4D-715B080B5FE2")
    override val value: String = "oil_change"
  }

  case object TireRotation extends MaintenanceType {
    override val id: UUID      = UUID.fromString("A9E7E8A5-03D3-4330-9CE6-5E176A1176B9")
    override val value: String = "tire_rotation"
  }

  case object BrakeCheck extends MaintenanceType {
    override val id: UUID      = UUID.fromString("2C7274E4-B340-42DA-B9D9-D7A60DB9F379")
    override val value: String = "brake_check"
  }

  case object EngineCheck extends MaintenanceType {
    override val id: UUID      = UUID.fromString("3C7F64C2-AF13-4D3B-8D38-54530FE89597")
    override val value: String = "engine_check"
  }

  case object BatteryCheck extends MaintenanceType {
    override val id: UUID      = UUID.fromString("E41ADD0C-D8BA-43B0-A870-4696BC0D67C1")
    override val value: String = "battery_check"
  }

  case object TransmissionCheck extends MaintenanceType {
    override val id: UUID      = UUID.fromString("C57B04D3-6401-4604-A4A6-6D1534A96002")
    override val value: String = "transmission_check"
  }

  case object RadiatorCheck extends MaintenanceType {
    override val id: UUID      = UUID.fromString("0779AFAE-D82B-4047-B63E-F94682504CF8")
    override val value: String = "radiator_check"
  }

  case object AirConditioningCheck extends MaintenanceType {
    override val id: UUID      = UUID.fromString("5526494F-F72B-4220-A98D-7020B6D1A478")
    override val value: String = "air_conditioning_check"
  }

  case object Other extends MaintenanceType {
    override val id: UUID      = UUID.fromString("23796DC5-4012-49F5-BE24-ECE0D69CBF64")
    override val value: String = "other"
  }

  private val values: Set[MaintenanceType] = Set(
    OilChange,
    TireRotation,
    BrakeCheck,
    EngineCheck,
    BatteryCheck,
    TransmissionCheck,
    RadiatorCheck,
    AirConditioningCheck,
    Other
  )

  def fromUUID(id: UUID): MaintenanceType = values
    .find(_.id == id)
    .getOrElse(
      throw new IllegalArgumentException(s"Invalid maintenance type: $id")
    )

  def fromString(str: String): MaintenanceType = values
    .find(_.value == str)
    .getOrElse(
      throw new IllegalArgumentException(s"Invalid maintenance type: $str")
    )

  given Encoder[MaintenanceType] = deriveEncoder

  given Decoder[MaintenanceType] = deriveDecoder
}

class MaintenanceTypeJsonSerializer extends StdSerializer[MaintenanceType](classOf[MaintenanceType]) {

  override def serialize(value: MaintenanceType, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeString(value.value)
}

class MaintenanceTypeJsonDeserializer extends StdDeserializer[MaintenanceType](classOf[MaintenanceType]) {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): MaintenanceType = {
    if (p.getCurrentToken == JsonToken.VALUE_NULL) {
      throw new IllegalArgumentException("Null value is not allowed")
    }

    val text = p.getText
    if (text == null || text.isEmpty) {
      throw new IllegalArgumentException("Empty value is not allowed")
    }

    MaintenanceType.fromString(text)
  }

  override def getNullValue(ctxt: DeserializationContext): MaintenanceType =
    throw new IllegalArgumentException("Null value is not allowed")
}
