package http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.JavaUUID
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import domain.model.api.car.get.{GetCarByIdResponse, GetCarsResponse}
import domain.model.api.car.post.{CreateCarRequest, CreateCarResponse}
import persistence.repository.CarRepository

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Car extends FailFastCirceSupport {

  import io.circe.generic.auto._
  // needed to make the Year type work with Circe
  import http.codec.YearCodec._

  def routes(carRepository: CarRepository)(implicit ec: ExecutionContext): Route = {
    concat(
      pathPrefix("car") {
        concat(
          pathEnd {
            concat(
              post {
                entity(as[CreateCarRequest]) { carInfo =>
                  val id = UUID.randomUUID()
                  val currentDate = LocalDateTime.now();
                  val newCar = new persistence.model.car.Car(id, carInfo.make, carInfo.model, carInfo.year, currentDate, Some(currentDate))

                  onComplete(carRepository.create(newCar)) {
                    case Success(value) => complete(StatusCodes.Created, value: CreateCarResponse)
                    case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred while creating a car: ${ex.getMessage}")
                  }
                }
              }
            )
          },
          path(JavaUUID) { id =>
            get {
              onComplete(carRepository.getById(id)) {
                case Success(car) => complete(StatusCodes.OK, car: GetCarByIdResponse)
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
              }
            }
          }
        )
      },
      // todo: add filtering
      pathPrefix("cars") {
        pathEnd {
          get {
            onComplete(carRepository.getAll) {
              case Success(cars) => complete(StatusCodes.OK, cars: GetCarsResponse)
              case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
            }
          }
        }
      }
    )
  }
}