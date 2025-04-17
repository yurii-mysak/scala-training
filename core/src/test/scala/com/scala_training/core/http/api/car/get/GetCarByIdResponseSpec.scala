package com.scala_training.core.http.api.car.get

import com.scala_training.core.domain.model.CarCore
import com.scala_training.core.persistence.command.CommandResponse
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.{LocalDateTime, Year}
import java.util.UUID

case class CarTest(
  id: UUID,
  make: String,
  model: String,
  year: Year,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime] = None
) extends CarCore

class GetCarByIdResponseSpec extends AnyFlatSpec with Matchers {
  private val testDateTime = LocalDateTime.of(2024, 3, 15, 12, 30, 0)

  private val testCar = new CarTest(
    id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
    make = "Toyota",
    model = "Camry",
    year = Year.of(2024),
    createdAt = testDateTime,
    updatedAt = Some(testDateTime)
  )

  private val expectedResponse = GetCarByIdResponse(
    id = testCar.id,
    make = testCar.make,
    model = testCar.model,
    year = testCar.year
  )

  "GetCarByIdResponse" should "convert from successful CommandResponse with car" in {
    val commandResponse                       = CommandResponse.Success(Some(testCar))
    val response: GetCarByIdResponse[CarTest] = commandResponse

    response should be(expectedResponse)
  }

  it should "throw IllegalStateException when CommandResponse is Success with None" in {
    val commandResponse = CommandResponse.Success(Option.empty[CarTest])

    val exception = intercept[IllegalStateException] {
      commandResponse: GetCarByIdResponse[CarTest]
    }
    exception.getMessage should be("Car was not found")
  }

  it should "throw IllegalStateException when CommandResponse is Failure" in {
    val errorMessage    = "Database error"
    val commandResponse = CommandResponse.Failure[CarTest](errorMessage)

    val exception = intercept[IllegalStateException] {
      commandResponse: GetCarByIdResponse[CarTest]
    }
    exception.getMessage should be(errorMessage)
  }

  it should "create response with correct fields" in {
    val response = GetCarByIdResponse(
      id = testCar.id,
      make = testCar.make,
      model = testCar.model,
      year = testCar.year
    )

    response.id should be(testCar.id)
    response.make should be(testCar.make)
    response.model should be(testCar.model)
    response.year should be(testCar.year)
  }
}
