package com.scala_training.akka.http.api.car.get

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import persistence.model.car.Car

import java.time.{LocalDateTime, Year}
import java.util.UUID

class GetCarByIdResponseSpec extends AnyFlatSpec with Matchers {
  private val testDateTime = LocalDateTime.of(2024, 3, 15, 12, 30, 0)

  private val testCar = new Car(
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
    val commandResponse                   = CommandResponse.Success(Some(testCar))
    val response: GetCarByIdResponse[Car] = commandResponse

    response should be(expectedResponse)
  }

  it should "throw IllegalStateException when CommandResponse is Success with None" in {
    val commandResponse = CommandResponse.Success(Option.empty[Car])

    val exception = intercept[IllegalStateException] {
      commandResponse: GetCarByIdResponse[Car]
    }
    exception.getMessage should be("Car was not found")
  }

  it should "throw IllegalStateException when CommandResponse is Failure" in {
    val errorMessage    = "Database error"
    val commandResponse = CommandResponse.Failure[Car](errorMessage)

    val exception = intercept[IllegalStateException] {
      commandResponse: GetCarByIdResponse[Car]
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
