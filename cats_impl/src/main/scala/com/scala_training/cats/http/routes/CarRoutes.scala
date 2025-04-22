package com.scala_training.cats.http.routes

import cats.effect.Concurrent
import cats.implicits.*
import com.scala_training.cats.application.CarService
import com.scala_training.cats.http.handlers
import com.scala_training.core.http.api.car.post.CreateCarRequest
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

class CarRoutes[F[_]: {Concurrent, Logger}](
  carService: CarService[F],
  errorHandler: handlers.HttpErrorHandler[F],
  responseHandler: handlers.CarResponseHandler[F]
) extends Http4sDsl[F] {
  import io.circe.generic.auto.*

  given createCarReqDecoder: EntityDecoder[F, CreateCarRequest] = jsonOf[F, CreateCarRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "api" / "car" =>
      (for {
        _      <- Logger[F].info("Attempting to create new car")
        carReq <- req.as[CreateCarRequest]
        car    <- carService.createCar(carReq).attempt
        result <- responseHandler.handleCreateResponse(car)
      } yield result).handleErrorWith(errorHandler.handleError("Unexpected error during car creation"))

    case GET -> Root / "api" / "car" / UUIDVar(id) =>
      (for {
        _      <- Logger[F].info(s"Fetching car with id: $id")
        result <- carService.getCar(id).attempt
        resp   <- responseHandler.handleGetResponse(result)
      } yield resp).handleErrorWith(errorHandler.handleError("Unexpected error during car fetch"))

    case GET -> Root / "api" / "cars" =>
      (for {
        _      <- Logger[F].info("Fetching all cars")
        result <- carService.getAllCars.attempt
        resp   <- responseHandler.handleGetAllResponse(result)
      } yield resp).handleErrorWith(errorHandler.handleError("Unexpected error during cars fetch"))
  }
}
