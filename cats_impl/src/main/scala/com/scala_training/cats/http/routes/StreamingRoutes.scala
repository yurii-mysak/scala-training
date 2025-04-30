package com.scala_training.cats.http.routes

import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.syntax.all.*
import fs2.Stream
import com.scala_training.core.persistence.command.CommandResponse
import com.scala_training.cats.persistence.repository.CarRepository
import com.scala_training.cats.streaming.ReflectiveStreamProcessor

import scala.concurrent.duration.*

object StreamingRoutes {

  def apply[F[_]: Async](carRepository: CarRepository[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val processor = new ReflectiveStreamProcessor

    HttpRoutes.of[F] { case GET -> Root / "api" / "cars" / "stream" =>
      for {
        carsResponse <- carRepository.getAll
        response     <- carsResponse match {
                          case CommandResponse.Success(Some(cars)) =>
                            val stream = Stream
                              .emits(cars.values.toSeq)
                              // Simulate processing delay to demonstrate streaming
                              .evalTap(_ => Async[F].sleep(1.second))
                              .zipWithIndex
                              .map(_._1)
                              // Use the processor to transform the stream items
                              .through(processor.processWithReflection)
                              .through(fs2.text.utf8.encode)
                            Ok(stream)
                          case CommandResponse.Success(None)       =>
                            Ok(Stream.empty.through(fs2.text.utf8.encode))
                          case CommandResponse.Failure(reason)     =>
                            InternalServerError(reason)
                        }
      } yield response
    }
  }
}
