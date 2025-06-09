package com.scala_training.cats.persistence.repository

import cats.effect.Concurrent
import cats.implicits.*
import com.scala_training.cats.persistence.model.Car
import doobie.Meta
import com.scala_training.core.persistence.command.CommandResponse
import com.scala_training.kafka.client.KafkaClient
import com.scala_training.kafka.model.CarEvent
import com.scala_training.kafka.model.CarEvent.given
import doobie.util.transactor.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

import java.time.Year
import java.util.UUID

trait CarRepositoryAPI[F[_]] {
  def create(car: Car): F[CommandResponse[Car]]
  def get(id: UUID): F[CommandResponse[Car]]
  def getAll: F[CommandResponse[Map[UUID, Car]]]
}

class CarRepository[F[_]: {Concurrent, LoggerFactory}](xa: Transactor[F], kafkaClient: KafkaClient[F], topic: String)
  extends CarRepositoryAPI[F] {
  given yearMeta: Meta[Year] = Meta[Int].imap(Year.of)(_.getValue)
  given logger: Logger[F]    = LoggerFactory[F].getLogger

  override def create(car: Car): F[CommandResponse[Car]] = (for {
    _ <- sql"""
        INSERT INTO cars (id, make, model, year, created_at, updated_at)
        VALUES (${car.id}, ${car.make}, ${car.model}, ${car.year}, ${car.createdAt}, ${car.updatedAt})
      """.update.run
           .transact(xa)
    _ <- kafkaClient.produce(topic, CarEvent.CarCreated(car)).compile.drain
  } yield car).attempt
    .flatTap {
      case Right(_) =>
        Logger[F].info(s"Successfully created car ${car.id}")
      case Left(e)  =>
        Logger[F].error(s"Failed to create car ${car.id}: ${e.getMessage}")
    }
    .map {
      case Right(_) => CommandResponse.Success(Some(car))
      case Left(e)  => CommandResponse.Failure(e.getMessage)
    }

  override def get(id: UUID): F[CommandResponse[Car]] = sql"""
      SELECT id, make, model, year, created_at, updated_at
      FROM cars
      WHERE id = $id
    """
    .query[Car]
    .option
    .transact(xa)
    .attempt
    .flatTap {
      case Right(Some(_)) => Logger[F].info(s"Successfully retrieved car $id")
      case Right(None)    => Logger[F].warn(s"Car $id not found")
      case Left(e)        => Logger[F].error(s"Error retrieving car $id: ${e.getMessage}")
    }
    .map {
      case Right(Some(car)) => CommandResponse.Success(Some(car))
      case Right(None)      => CommandResponse.Failure("Car not found")
      case Left(e)          => CommandResponse.Failure(e.getMessage)
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
    .attempt
    .flatTap {
      case Right(cars) => Logger[F].info(s"Successfully retrieved ${cars.size} cars")
      case Left(e)     => Logger[F].error(s"Failed to retrieve cars: ${e.getMessage}")
    }
    .map {
      case Right(cars) => CommandResponse.Success(Some(cars.map(m => m.id -> m).toMap))
      case Left(e)     => CommandResponse.Failure(e.getMessage)
    }
}
