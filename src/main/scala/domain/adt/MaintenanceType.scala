package domain.adt

import akka.serialization.jackson.JsonSerializable
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.util.UUID

@JsonSerialize(`using` = classOf[MaintenanceTypeJsonSerializer])
@JsonDeserialize(`using` = classOf[MaintenanceTypeJsonDeserializer])
sealed trait MaintenanceType extends JsonSerializable {
  def id: UUID
}

object MaintenanceType {
  case object OilChange extends MaintenanceType {
    override val id: UUID = UUID.fromString("10A6A1EC-0794-4778-AE4D-715B080B5FE2")
  }

  case object TireRotation extends MaintenanceType {
    override val id: UUID = UUID.fromString("A9E7E8A5-03D3-4330-9CE6-5E176A1176B9")
  }

  case object BrakeCheck extends MaintenanceType {
    override val id: UUID = UUID.fromString("2C7274E4-B340-42DA-B9D9-D7A60DB9F379")
  }

  case object EngineCheck extends MaintenanceType {
    override val id: UUID = UUID.fromString("3C7F64C2-AF13-4D3B-8D38-54530FE89597")
  }

  case object BatteryCheck extends MaintenanceType {
    override val id: UUID = UUID.fromString("E41ADD0C-D8BA-43B0-A870-4696BC0D67C1")
  }

  case object TransmissionCheck extends MaintenanceType {
    override val id: UUID = UUID.fromString("C57B04D3-6401-4604-A4A6-6D1534A96002")
  }

  case object RadiatorCheck extends MaintenanceType {
    override val id: UUID = UUID.fromString("0779AFAE-D82B-4047-B63E-F94682504CF8")
  }

  case object AirConditioningCheck extends MaintenanceType {
    override val id: UUID = UUID.fromString("5526494F-F72B-4220-A98D-7020B6D1A478")
  }

  case object Other extends MaintenanceType {
    override val id: UUID = UUID.fromString("23796DC5-4012-49F5-BE24-ECE0D69CBF64")
  }

  private val values: Set[MaintenanceType] = Set(
    OilChange, TireRotation, BrakeCheck, EngineCheck, BatteryCheck,
    TransmissionCheck, RadiatorCheck, AirConditioningCheck, Other
  )

  def fromUUID(id: UUID): Option[MaintenanceType] = values.find(_.id == id)
}

class MaintenanceTypeJsonSerializer extends StdSerializer[MaintenanceType](classOf[MaintenanceType]) {

  import MaintenanceType._

  override def serialize(value: MaintenanceType, gen: JsonGenerator, provider: SerializerProvider): Unit = {
    val strValue = value match {
      case OilChange => "oil_change"
      case TireRotation => "tire_rotation"
      case BrakeCheck => "brake_check"
      case EngineCheck => "engine_check"
      case BatteryCheck => "battery_check"
      case TransmissionCheck => "transmission_check"
      case RadiatorCheck => "radiator_check"
      case AirConditioningCheck => "air_conditioning_check"
      case Other => "other"
    }
    gen.writeString(strValue)
  }
}

class MaintenanceTypeJsonDeserializer extends StdDeserializer[MaintenanceType](classOf[MaintenanceType]) {

  import MaintenanceType._

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): MaintenanceType = {
    p.getText match {
      case "oil_change" => OilChange
      case "tire_rotation" => TireRotation
      case "brake_check" => BrakeCheck
      case "engine_check" => EngineCheck
      case "battery_check" => BatteryCheck
      case "transmission_check" => TransmissionCheck
      case "radiator_check" => RadiatorCheck
      case "air_conditioning_check" => AirConditioningCheck
      case "other" => Other
      case unknown => throw new IllegalArgumentException(s"Unknown maintenance type: $unknown")
    }
  }
}
