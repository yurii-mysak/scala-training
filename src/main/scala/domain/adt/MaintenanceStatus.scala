package domain.adt

import akka.serialization.jackson.JsonSerializable
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

@JsonSerialize(`using` = classOf[MaintenanceStatusJsonSerializer])
@JsonDeserialize(`using` = classOf[MaintenanceStatusJsonDeserializer])
sealed trait MaintenanceStatus extends JsonSerializable

object MaintenanceStatus {
  case object Created extends MaintenanceStatus

  case object Scheduled extends MaintenanceStatus

  case object Pending extends MaintenanceStatus

  case object InProgress extends MaintenanceStatus

  case object Completed extends MaintenanceStatus

  case object Cancelled extends MaintenanceStatus
}

class MaintenanceStatusJsonSerializer extends StdSerializer[MaintenanceStatus](classOf[MaintenanceStatus]) {

  import MaintenanceStatus._

  override def serialize(value: MaintenanceStatus, gen: JsonGenerator, provider: SerializerProvider): Unit = {
    val strValue = value match {
      case Created => "created"
      case Scheduled => "scheduled"
      case Pending => "pending"
      case InProgress => "in_progress"
      case Completed => "completed"
      case Cancelled => "cancelled"
    }
    gen.writeString(strValue)
  }
}

class MaintenanceStatusJsonDeserializer extends StdDeserializer[MaintenanceStatus](classOf[MaintenanceStatus]) {

  import MaintenanceStatus._

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): MaintenanceStatus = {
    p.getText match {
      case "created" => Created
      case "scheduled" => Scheduled
      case "pending" => Pending
      case "in_progress" => InProgress
      case "completed" => Completed
      case "cancelled" => Cancelled
      case unknown => throw new IllegalArgumentException(s"Unknown maintenance status: $unknown")
    }
  }
}