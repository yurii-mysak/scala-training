package com.scala_training.cats.persistence.model

import cats.implicits.catsSyntaxTuple6Semigroupal
import com.scala_training.core.domain.model.CarCore
import doobie.util.{Read, Write}
import doobie.postgres.implicits.*

import java.util.UUID
import java.time.{LocalDateTime, Year}

case class Car(
  id: UUID,
  make: String,
  model: String,
  year: Year,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime] = None
) extends CarCore

object Car {
  given yearWrite: Write[Year] = Write[Int].contramap(_.getValue)
  given yearRead: Read[Year]   = Read[Int].map(Year.of)

  given carRead: Read[Car] = (
    Read[UUID],
    Read[String],
    Read[String],
    Read[Year],
    Read[LocalDateTime],
    Read[Option[LocalDateTime]]
  ).mapN(apply)

  given carWrite: Write[Car] = (
    Write[UUID],
    Write[String],
    Write[String],
    Write[Year],
    Write[LocalDateTime],
    Write[Option[LocalDateTime]]
  ).tupled.contramap { car =>
    (car.id, car.make, car.model, car.year, car.createdAt, car.updatedAt)
  }
}
