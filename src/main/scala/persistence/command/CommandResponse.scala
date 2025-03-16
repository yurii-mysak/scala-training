package com.github.scala_training
package persistence.command

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import spray.json.*
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.github.scala_training.http.mapping.CarJsonProtocol.*

// Responses
enum CommandResponse[Model] {
  case Success(model: Option[Model])
  case Failure(reason: String)
}

object CommandResponse {
  implicit def toHttpResponse[Model: JsonFormat](response: CommandResponse[Model]): ToResponseMarshallable = {
    response match {
      case CommandResponse.Success(Some(model)) => HttpResponse(StatusCodes.OK, entity = model.toJson().toString())
      case CommandResponse.Success(None) => HttpResponse(StatusCodes.NotFound, entity = "Not found")
      case CommandResponse.Failure(reason) => HttpResponse(StatusCodes.BadRequest, entity = reason)
    }
  }
}