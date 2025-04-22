package com.scala_training.cats.config

import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

case class AppConfig(
  server: ServerConfig,
  database: DatabaseConfig
) derives ConfigReader

case class ServerConfig(
  host: String,
  port: Int,
  timeout: FiniteDuration
)

case class DatabaseConfig(
  driver: String,
  url: String,
  user: String,
  password: String
)
