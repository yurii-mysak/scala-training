package com.scala_training.kafka.client

import com.scala_training.kafka.model.KafkaEvent
import cats.effect.Async
import cats.syntax.all.*
import fs2.kafka.*
import fs2.Stream
import io.circe.{Decoder, Encoder, parser}
import io.circe.syntax.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

import java.util.UUID

class KafkaCatsClient[F[_]: {Async, LoggerFactory}](
  bootstrap: String,
  retries: Int = 3
):
  val logger: Logger[F] = LoggerFactory[F].getLogger

  private def valueSerializer[A: Encoder]: ValueSerializer[F, A] = Serializer.instance[F, A] { (_, _, event) =>
    Async[F].pure(event.asJson.noSpaces.getBytes("UTF-8"))
  }

  private def valueDeserializer[A: Decoder]: ValueDeserializer[F, A] = Deserializer.instance[F, A] { (_, _, bytes) =>
    Async[F].fromEither(
      parser.decode[A](new String(bytes, "UTF-8"))
    )
  }

  private def makeProducer[A: Encoder] = KafkaProducer.stream(
    ProducerSettings(keySerializer = Serializer.string[F], valueSerializer = valueSerializer[A])
      .withBootstrapServers(bootstrap)
      .withRetries(retries)
      .withEnableIdempotence(true)
  )

  def produce[A <: KafkaEvent: Encoder](topic: String, value: A): Stream[F, Unit] = makeProducer[A]
    .evalMap(producer =>
      producer
        .produceOne(ProducerRecord(topic, UUID.randomUUID().toString, value))
        .flatten
        .flatTap(_ =>
          logger
            .info(s"Successfully produced event: ${value.getClass.getSimpleName} with id ${value.id} to topic $topic")
        )
    )
    .void

  def stream[A: Decoder](topic: String, groupId: String): Stream[F, CommittableConsumerRecord[F, String, A]] =
    KafkaConsumer
      .stream(
        ConsumerSettings(keyDeserializer = Deserializer.string[F], valueDeserializer = valueDeserializer[A])
          .withBootstrapServers(bootstrap)
          .withGroupId(groupId)
          .withAutoOffsetReset(AutoOffsetReset.Earliest)
      )
      .subscribeTo(topic)
      .records
