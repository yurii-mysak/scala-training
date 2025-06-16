package com.scala_training.kafka.client

import com.scala_training.kafka.model.KafkaEvent
import akka.Done
import akka.actor.typed.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer, SendProducer}
import akka.kafka.{CommitterSettings, ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink, Source}
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import io.circe.syntax.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class KafkaAkkaClient(bootstrap: String)(
  implicit system: ActorSystem[?]
) {
  private val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")

  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrap)
  private val producer         = SendProducer(producerSettings)

  private def createConsumerSettings(groupId: String) =
    ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrap)
      .withGroupId(groupId)

  def produce[A <: KafkaEvent: Encoder](topic: String, value: A): Future[Unit] = {
    val record = new ProducerRecord(topic, UUID.randomUUID().toString, value.asJson.noSpaces)

    producer
      .send(record)
      .map { metadata =>
        system.log.info(
          s"Successfully produced event: ${value.getClass.getSimpleName} with id ${value.id} to topic $topic at offset ${metadata.offset()}"
        )
      }(system.executionContext)
      .recoverWith { case e =>
        system.log.error(
          s"Failed to produce event: ${value.getClass.getSimpleName} with id ${value.id} to topic $topic: ${e.getMessage}"
        )
        Future.failed(e)
      }(system.executionContext)
  }

  def stream[A: Decoder](topic: String, groupId: String): Source[A, Consumer.Control] = Consumer
    .committableSource(createConsumerSettings(groupId), Subscriptions.topics(topic))
    .mapAsync(1) { msg =>
      Try(io.circe.parser.decode[A](msg.record.value())) match {
        case Success(Right(value)) =>
          system.log.debug(s"Successfully decoded message from topic $topic")
          Future.successful(value)
        case Success(Left(error))  =>
          system.log.error(s"Failed to decode message from topic $topic: ${error.getMessage}")
          Future.failed(error)
        case Failure(error)        =>
          system.log.error(s"Failed to parse message from topic $topic: ${error.getMessage}")
          Future.failed(error)
      }
    }

  def close(): Future[Done] = producer.close()
}
