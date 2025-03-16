package com.github.scala_training
package http.mapping

import domain.model.api.post.car.{CreateCarRequest, CreateCarResponse}

import com.github.scala_training.http.mapping.format.UUIDJsonFormat
import com.github.scala_training.http.mapping.format.LocalDateTimeJsonFormat
import com.github.scala_training.persistence.model.car.Car
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object CarJsonProtocol extends DefaultJsonProtocol {
  given createCarRequestFormat: RootJsonFormat[CreateCarRequest] = jsonFormat3(CreateCarRequest.apply)
  given createCarResponseFormat: RootJsonFormat[CreateCarResponse] = jsonFormat2(CreateCarResponse.apply)
  given carFormat: RootJsonFormat[Car] = jsonFormat6(Car.apply)
}