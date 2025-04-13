package application

import cats.effect.Sync
import http.api.car.post.CreateCarRequest
import persistence.command.CommandResponse
import persistence.model.Car
import persistence.repository.CarRepositoryAPI

import java.time.LocalDateTime
import java.util.UUID

class CarService[F[_]: Sync](repository: CarRepositoryAPI[F]) {

  def createCar(req: CreateCarRequest): F[CommandResponse[Car]] = {
    val now = LocalDateTime.now()
    val car = Car(UUID.randomUUID(), req.make, req.model, req.year, now, Some(now))
    repository.create(car)
  }

  def getCar(id: UUID): F[CommandResponse[Car]] = repository.get(id)

  def getAllCars: F[CommandResponse[Map[UUID, Car]]] = repository.getAll
}
