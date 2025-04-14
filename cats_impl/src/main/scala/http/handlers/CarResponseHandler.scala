package http.handlers

import cats.effect.Sync
import cats.implicits._
import domain.model.CarCore
import http.api.car.get.{GetCarByIdResponse, GetCarsResponse}
import http.api.car.post.CreateCarResponse
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.{EntityEncoder, Response}
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import persistence.command.CommandResponse

import java.util.UUID

class CarResponseHandler[F[_]: Sync: Logger] extends Http4sDsl[F] {
  import io.circe.generic.auto._
  import http.codec.YearCodec._

  implicit def createCarRespEncoder[A <: CarCore]: EntityEncoder[F, CreateCarResponse[A]] =
    jsonEncoderOf[F, CreateCarResponse[A]]

  implicit def getCarByIdRespEncoder[A <: CarCore]: EntityEncoder[F, GetCarByIdResponse[A]] =
    jsonEncoderOf[F, GetCarByIdResponse[A]]

  implicit def getCarsRespEncoder[A <: CarCore]: EntityEncoder[F, GetCarsResponse[A]] =
    jsonEncoderOf[F, GetCarsResponse[A]]

  def handleCreateResponse[A <: CarCore](result: Either[Throwable, CommandResponse[A]]): F[Response[F]] = result match {
    case Right(response) =>
      response match {
        case CommandResponse.Success(Some(car)) =>
          Logger[F].info(s"Successfully created car with id: ${car.id}") *> Created(response: CreateCarResponse[A])
        case CommandResponse.Success(None)      =>
          Conflict("Car creation failed")
        case CommandResponse.Failure(reason)    =>
          Conflict(reason)
      }
    case Left(err)       =>
      Logger[F].error(s"Failed to create car: ${err.getMessage}") *> InternalServerError()
  }

  def handleGetResponse[A <: CarCore](result: Either[Throwable, CommandResponse[A]]): F[Response[F]] = result match {
    case Right(carByIdCommandResponse @ CommandResponse.Success(Some(_))) =>
      Ok(carByIdCommandResponse: GetCarByIdResponse[A])
    case Right(CommandResponse.Success(None))                             =>
      NotFound("Car not found")
    case Right(CommandResponse.Failure(reason))                           =>
      NotFound(reason)
    case Left(err)                                                        =>
      Logger[F].error(s"Error fetching car: ${err.getMessage}") *> InternalServerError()
  }

  def handleGetAllResponse[A <: CarCore](result: Either[Throwable, CommandResponse[Map[UUID, A]]]): F[Response[F]] =
    result match {
      case Right(response) => Ok(response: GetCarsResponse[A])
      case Left(err)       =>
        Logger[F].error(s"Failed to fetch cars: ${err.getMessage}") *> InternalServerError()
    }
}
