package http.routes

import cats.effect.Sync
import cats.implicits._
import http.api.car.post.CreateCarRequest
import http.handlers.HttpErrorHandler
import http.handlers.CarResponseHandler
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import application.CarService

class CarRoutes[F[_]: Sync: Logger](
  carService: CarService[F],
  errorHandler: HttpErrorHandler[F],
  responseHandler: CarResponseHandler[F]
) extends Http4sDsl[F] {
  import http.codec.YearCodec._
  import io.circe.generic.auto._

  implicit def createCarReqDecoder: EntityDecoder[F, CreateCarRequest] = jsonOf[F, CreateCarRequest]

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
