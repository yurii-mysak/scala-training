package com.scala_training.kafka.model

import java.time.LocalDateTime
import java.util.UUID

trait KafkaEvent {
  def id: UUID                 = UUID.randomUUID()
  def timestamp: LocalDateTime = LocalDateTime.now()
}
