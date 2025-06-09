package com.scala_training.core.config

import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

case class AppConfig(
  server: ServerConfig,
  database: Option[DatabaseConfig],
  kafka: KafkaConfig
) derives ConfigReader

case class ServerConfig(
  host: String,
  port: Int,
  timeout: Option[FiniteDuration]
) derives ConfigReader

case class DatabaseConfig(
  driver: String,
  url: String,
  user: String,
  password: String
) derives ConfigReader

case class KafkaConfig(
  bootstrapServers: String,
  carEventsTopic: String,
  maintenanceEventsTopic: String,
  groupId: Option[String],
  retries: Int
) derives ConfigReader
