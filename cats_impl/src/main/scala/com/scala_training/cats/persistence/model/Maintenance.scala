package com.scala_training.cats.persistence.model

import cats.implicits.catsSyntaxTuple8Semigroupal
import com.scala_training.core.domain.model.MaintenanceCore
import com.scala_training.core.domain.adt.{MaintenanceStatus, MaintenanceType}
import com.scala_training.core.domain.adt.MaintenanceType.given
import com.scala_training.core.domain.adt.MaintenanceStatus.given
import doobie.util.{Read, Write}
import doobie.postgres.implicits.*
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.util.UUID
import java.time.LocalDateTime

case class Maintenance(
  id: UUID,
  carId: UUID,
  description: String,
  maintenanceTypes: List[MaintenanceType],
  scheduledDate: Option[LocalDateTime] = None,
  status: MaintenanceStatus,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime] = None
) extends MaintenanceCore

object Maintenance {

  given statusRead: Read[MaintenanceStatus]          = Read[String].map { str =>
    MaintenanceStatus
      .fromString(str)
  }
  given statusWrite: Write[MaintenanceStatus]        = Write[String].contramap(_.toString)
  given maintenanceTypeRead: Read[MaintenanceType]   = Read[UUID].map(MaintenanceType.fromUUID)
  given maintenanceTypeWrite: Write[MaintenanceType] = Write[UUID].contramap(_.id)

  given maintenanceTypesRead: Read[List[MaintenanceType]] =
    Read[Array[UUID]].map(_.toList.map(MaintenanceType.fromUUID))

  given maintenanceTypesWrite: Write[List[MaintenanceType]] = Write[Array[UUID]].contramap(_.map(_.id).toArray)

  given maintenanceRead: Read[Maintenance] = (
    Read[UUID],
    Read[UUID],
    Read[String],
    Read[List[MaintenanceType]],
    Read[Option[LocalDateTime]],
    Read[MaintenanceStatus],
    Read[LocalDateTime],
    Read[Option[LocalDateTime]]
  )
    .mapN(apply)

  given maintenanceWrite: Write[Maintenance] = (
    Write[UUID],
    Write[UUID],
    Write[String],
    Write[List[MaintenanceType]],
    Write[Option[LocalDateTime]],
    Write[MaintenanceStatus],
    Write[LocalDateTime],
    Write[Option[LocalDateTime]]
  ).tupled.contramap { maintenance =>
    (
      maintenance.id,
      maintenance.carId,
      maintenance.description,
      maintenance.maintenanceTypes,
      maintenance.scheduledDate,
      maintenance.status,
      maintenance.createdAt,
      maintenance.updatedAt
    )
  }

  given Encoder[Maintenance] = deriveEncoder

  given Decoder[Maintenance] = deriveDecoder
}
