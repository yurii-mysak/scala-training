package com.scala_training.core.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.scala_training.core.domain.adt.{
  MaintenanceType,
  MaintenanceTypeJsonDeserializer,
  MaintenanceTypeJsonSerializer
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MaintenanceTypeSpec extends AnyFlatSpec with Matchers {
  private val mapper = new ObjectMapper()

  private val module = new SimpleModule()
    .addSerializer(new MaintenanceTypeJsonSerializer())
    .addDeserializer(classOf[MaintenanceType], new MaintenanceTypeJsonDeserializer())

  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(module)

  "MaintenanceType" should "serialize all type values correctly" in {
    val types = Map(
      MaintenanceType.OilChange            -> s"\"${MaintenanceType.OilChange.value}\"",
      MaintenanceType.TireRotation         -> s"\"${MaintenanceType.TireRotation.value}\"",
      MaintenanceType.BrakeCheck           -> s"\"${MaintenanceType.BrakeCheck.value}\"",
      MaintenanceType.EngineCheck          -> s"\"${MaintenanceType.EngineCheck.value}\"",
      MaintenanceType.BatteryCheck         -> s"\"${MaintenanceType.BatteryCheck.value}\"",
      MaintenanceType.TransmissionCheck    -> s"\"${MaintenanceType.TransmissionCheck.value}\"",
      MaintenanceType.RadiatorCheck        -> s"\"${MaintenanceType.RadiatorCheck.value}\"",
      MaintenanceType.AirConditioningCheck -> s"\"${MaintenanceType.AirConditioningCheck.value}\"",
      MaintenanceType.Other                -> s"\"${MaintenanceType.Other.value}\""
    )

    types.foreach { case (maintenanceType, expected) =>
      mapper.writeValueAsString(maintenanceType) should be(expected)
    }
  }

  it should "deserialize all type values correctly" in {
    val types = Map(
      s"\"${MaintenanceType.OilChange.value}\""            -> MaintenanceType.OilChange,
      s"\"${MaintenanceType.TireRotation.value}\""         -> MaintenanceType.TireRotation,
      s"\"${MaintenanceType.BrakeCheck.value}\""           -> MaintenanceType.BrakeCheck,
      s"\"${MaintenanceType.EngineCheck.value}\""          -> MaintenanceType.EngineCheck,
      s"\"${MaintenanceType.BatteryCheck.value}\""         -> MaintenanceType.BatteryCheck,
      s"\"${MaintenanceType.TransmissionCheck.value}\""    -> MaintenanceType.TransmissionCheck,
      s"\"${MaintenanceType.RadiatorCheck.value}\""        -> MaintenanceType.RadiatorCheck,
      s"\"${MaintenanceType.AirConditioningCheck.value}\"" -> MaintenanceType.AirConditioningCheck,
      s"\"${MaintenanceType.Other.value}\""                -> MaintenanceType.Other
    )

    types.foreach { case (json, expected) =>
      mapper.readValue(json, classOf[MaintenanceType]) should be(expected)
    }
  }

  it should "fail to deserialize invalid type" in {
    val invalidJson = "\"invalid_type\""
    an[IllegalArgumentException] should be thrownBy
      mapper.readValue(invalidJson, classOf[MaintenanceType])
  }

  it should "fail to deserialize uppercase type values" in {
    val types = List(
      MaintenanceType.OilChange.value,
      MaintenanceType.TireRotation.value,
      MaintenanceType.BrakeCheck.value,
      MaintenanceType.EngineCheck.value,
      MaintenanceType.BatteryCheck.value,
      MaintenanceType.TransmissionCheck.value,
      MaintenanceType.RadiatorCheck.value,
      MaintenanceType.AirConditioningCheck.value,
      MaintenanceType.Other.value
    )

    types.foreach { value =>
      an[IllegalArgumentException] should be thrownBy
        mapper.readValue(s"\"${value.toUpperCase}\"", classOf[MaintenanceType])
    }
  }

  it should "maintain value through serialization and deserialization" in {
    val original     = MaintenanceType.EngineCheck
    val json         = mapper.writeValueAsString(original)
    val deserialized = mapper.readValue(json, classOf[MaintenanceType])
    deserialized should be(original)
  }

  it should "fail to deserialize null and empty values" in {
    val nullJson  = "null"
    val emptyJson = "\"\""

    a[IllegalArgumentException] should be thrownBy
      mapper.readValue(nullJson, classOf[MaintenanceType])

    an[IllegalArgumentException] should be thrownBy
      mapper.readValue(emptyJson, classOf[MaintenanceType])
  }
}
