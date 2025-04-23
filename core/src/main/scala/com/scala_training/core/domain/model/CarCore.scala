package com.scala_training.core.domain.model

import java.time.{LocalDateTime, Year}
import java.util.UUID

trait CarCore {
  val id: UUID
  val make: String
  val model: String
  val year: Year
  val createdAt: LocalDateTime
  val updatedAt: Option[LocalDateTime]
}
