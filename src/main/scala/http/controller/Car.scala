package com.github.scala_training
package http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import spray.json.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import domain.model.api.post.car.CreateCarRequest
import http.mapping.CarJsonProtocol.given

import com.github.scala_training.persistence.model.car.Car
import com.github.scala_training.persistence.repository.CarRepository
import com.github.scala_training.persistence.command.CommandResponse.*

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import java.util.UUID

object Car {
  def routes(carRepository: CarRepository)(using ec: ExecutionContext): Route = {
    pathPrefix("car") {
      concat(
        pathEnd {
          concat(
            get {
              // todo: Implement logic to get all cars
              complete(StatusCodes.OK, "Get all cars")
            },
            post {
              entity(as[CreateCarRequest]) { carInfo =>
                val id = UUID.randomUUID()
                val currentDate = LocalDateTime.now();
                val newCar = new persistence.model.car.Car(id, carInfo.make, carInfo.model, carInfo.year, currentDate, Some(currentDate))

                onComplete(carRepository.create(newCar)) {
                  case Success(value) => complete(value)
                  case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred while creating a car: ${ex.getMessage}")
                }
              }
            }
          )
        },
        path(Segment) { id =>
          get {
            // todo: Implement logic to get a single car by ID
            complete(StatusCodes.OK, s"Get car with ID: $id")
          }
        }
      )
    }
  }
}