package com.scala_training.akka.config

import com.scala_training.core.config.{AppConfig, KafkaConfig, ServerConfig}
import com.typesafe.config.ConfigFactory

object Loader:

  def load(): AppConfig =
    val config = ConfigFactory.load()

    AppConfig(
      server = ServerConfig(
        host = config.getString("server.host"),
        port = config.getInt("server.port"),
        timeout = None
      ),
      kafka = KafkaConfig(
        bootstrapServers = config.getString("kafka.bootstrap-servers"),
        carEventsTopic = config.getString("kafka.car-events-topic"),
        maintenanceEventsTopic = config.getString("kafka.maintenance-events-topic"),
        retries = config.getInt("kafka.retries"),
        groupId = None
      ),
      database = None
    )
