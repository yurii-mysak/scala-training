package com.scala_training.core.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.scala_training.core.domain.adt.{
  MaintenanceStatus,
  MaintenanceStatusJsonDeserializer,
  MaintenanceStatusJsonSerializer
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MaintenanceStatusSpec extends AnyFlatSpec with Matchers {
  private val mapper = new ObjectMapper()

  private val module = new SimpleModule()
    .addSerializer(new MaintenanceStatusJsonSerializer())
    .addDeserializer(classOf[MaintenanceStatus], new MaintenanceStatusJsonDeserializer())

  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(module)

  "MaintenanceStatus" should "serialize all status values correctly" in {
    val statuses = Map(
      MaintenanceStatus.Created    -> s"\"${MaintenanceStatus.Created.value}\"",
      MaintenanceStatus.Scheduled  -> s"\"${MaintenanceStatus.Scheduled.value}\"",
      MaintenanceStatus.Pending    -> s"\"${MaintenanceStatus.Pending.value}\"",
      MaintenanceStatus.InProgress -> s"\"${MaintenanceStatus.InProgress.value}\"",
      MaintenanceStatus.Completed  -> s"\"${MaintenanceStatus.Completed.value}\"",
      MaintenanceStatus.Cancelled  -> s"\"${MaintenanceStatus.Cancelled.value}\""
    )

    statuses.foreach { case (status, expected) =>
      mapper.writeValueAsString(status) should be(expected)
    }
  }

  it should "deserialize all status values correctly" in {
    val statuses = Map(
      s"\"${MaintenanceStatus.Created.value}\""    -> MaintenanceStatus.Created,
      s"\"${MaintenanceStatus.Scheduled.value}\""  -> MaintenanceStatus.Scheduled,
      s"\"${MaintenanceStatus.Pending.value}\""    -> MaintenanceStatus.Pending,
      s"\"${MaintenanceStatus.InProgress.value}\"" -> MaintenanceStatus.InProgress,
      s"\"${MaintenanceStatus.Completed.value}\""  -> MaintenanceStatus.Completed,
      s"\"${MaintenanceStatus.Cancelled.value}\""  -> MaintenanceStatus.Cancelled
    )

    statuses.foreach { case (json, expected) =>
      mapper.readValue(json, classOf[MaintenanceStatus]) should be(expected)
    }
  }

  it should "fail to deserialize invalid status" in {
    val invalidJson = "\"invalid_status\""
    an[IllegalArgumentException] should be thrownBy
      mapper.readValue(invalidJson, classOf[MaintenanceStatus])
  }

  it should "fail to deserialize uppercase status values" in {
    val statuses = List(
      MaintenanceStatus.Created.value,
      MaintenanceStatus.Scheduled.value,
      MaintenanceStatus.Pending.value,
      MaintenanceStatus.InProgress.value,
      MaintenanceStatus.Completed.value,
      MaintenanceStatus.Cancelled.value
    )

    statuses.foreach { value =>
      an[IllegalArgumentException] should be thrownBy
        mapper.readValue(s"\"${value.toUpperCase}\"", classOf[MaintenanceStatus])
    }
  }

  it should "maintain value through serialization and deserialization" in {
    val original     = MaintenanceStatus.InProgress
    val json         = mapper.writeValueAsString(original)
    val deserialized = mapper.readValue(json, classOf[MaintenanceStatus])
    deserialized should be(original)
  }

  it should "fail to deserialize null and empty values" in {
    val nullJson  = "null"
    val emptyJson = "\"\""

    a[IllegalArgumentException] should be thrownBy
      mapper.readValue(nullJson, classOf[MaintenanceStatus])

    an[IllegalArgumentException] should be thrownBy
      mapper.readValue(emptyJson, classOf[MaintenanceStatus])
  }
}
