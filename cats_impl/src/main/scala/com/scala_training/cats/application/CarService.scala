package com.scala_training.cats.application

import cats.effect.Concurrent
import com.scala_training.cats.persistence.model.Car
import com.scala_training.cats.persistence.repository.CarRepositoryAPI
import com.scala_training.core.http.api.car.post.CreateCarRequest
import com.scala_training.core.persistence.command.CommandResponse

import java.time.LocalDateTime
import java.util.UUID

class CarService[F[_]: Concurrent](repository: CarRepositoryAPI[F]) {

  def createCar(req: CreateCarRequest): F[CommandResponse[Car]] = {
    val now = LocalDateTime.now()
    val car = Car(UUID.randomUUID(), req.make, req.model, req.year, now, Some(now))
    repository.create(car)
  }

  def getCar(id: UUID): F[CommandResponse[Car]] = repository.get(id)

  def getAllCars: F[CommandResponse[Map[UUID, Car]]] = repository.getAll
}
