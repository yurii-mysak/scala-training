package persistence.repository

import cats.effect.Sync
import cats.implicits._
import doobie.Meta
import persistence.command.CommandResponse
import doobie.util.transactor.Transactor
import doobie.implicits._
import persistence.model.Car
import doobie.postgres.implicits._

import java.time.Year
import java.util.UUID

trait CarRepositoryAPI[F[_]] {
  def create(car: Car): F[CommandResponse[Car]]
  def get(id: UUID): F[CommandResponse[Car]]
  def getAll: F[CommandResponse[Map[UUID, Car]]]
}

class CarRepository[F[_]: Sync](xa: Transactor[F]) extends CarRepositoryAPI[F] {
  implicit val yearMeta: Meta[Year] = Meta[Int].imap(Year.of)(_.getValue)

  override def create(car: Car): F[CommandResponse[Car]] = sql"""
      INSERT INTO cars (id, make, model, year, created_at, updated_at)
      VALUES (${car.id}, ${car.make}, ${car.model}, ${car.year}, ${car.createdAt}, ${car.updatedAt})
    """.update.run
    .transact(xa)
    .map[CommandResponse[Car]](_ => CommandResponse.Success(Some(car)))
    .handleErrorWith(err => Sync[F].pure(CommandResponse.Failure[Car](err.getMessage)))

  override def get(id: UUID): F[CommandResponse[Car]] = sql"""
      SELECT id, make, model, year, created_at, updated_at
      FROM cars
      WHERE id = $id
    """.query[Car].option.transact(xa).map {
    case Some(car) => CommandResponse.Success(Some(car))
    case None      => CommandResponse.Failure("Car not found")
  }

  override def getAll: F[CommandResponse[Map[UUID, Car]]] = sql"""
      SELECT id, make, model, year, created_at, updated_at
      FROM cars
    """
    .query[Car]
    .stream
    .compile
    .toList
    .transact(xa)
    .map(cars => CommandResponse.Success(Some(cars.map(car => car.id -> car).toMap)))
}
