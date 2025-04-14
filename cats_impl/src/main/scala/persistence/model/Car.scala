package persistence.model

import cats.implicits.catsSyntaxTuple6Semigroupal
import domain.model.CarCore
import doobie.util.{Read, Write}
import doobie.postgres.implicits._

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
  implicit val yearWrite: Write[Year] = Write[Int].contramap(_.getValue)
  implicit val yearRead: Read[Year]   = Read[Int].map(Year.of)

  implicit val carRead: Read[Car] = (
    Read[UUID],
    Read[String],
    Read[String],
    Read[Year],
    Read[LocalDateTime],
    Read[Option[LocalDateTime]]
  ).mapN(Car.apply _)

  implicit val carWrite: Write[Car] = (
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
