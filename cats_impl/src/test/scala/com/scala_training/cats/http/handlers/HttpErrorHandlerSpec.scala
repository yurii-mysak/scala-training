package com.scala_training.cats.http.handlers

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.scala_training.core.domain.adt.AppError
import org.http4s.Status
import org.scalacheck.Gen
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.parser.decode
import io.circe.generic.auto.*

class HttpErrorHandlerSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with ScalaCheckPropertyChecks {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val handler = new HttpErrorHandler[IO]

  // Generators for test data
  val errorMessageGen: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

  val appErrorGen: Gen[AppError] = for {
    message <- errorMessageGen
    error   <- Gen.oneOf(
                 AppError.NotFound(message),
                 AppError.BadRequest(message),
                 AppError.InternalError(message)
               )
  } yield error

  val throwableGen: Gen[Throwable] = for {
    message <- errorMessageGen
    error   <- Gen.oneOf(
                 new RuntimeException(message),
                 new IllegalArgumentException(message),
                 new IllegalStateException(message)
               )
  } yield error

  case class ErrorResponse(code: String, message: String)

  it should "handle AppErrors with correct status and structure in response" in
    forAll(appErrorGen) { error =>
      // NOTE: the only way I could run and execute tests, but I feel it is wrong and could be due to versions used
      val response       = handler.handleError(error.message)(error).unsafeRunSync()
      val expectedStatus = error match {
        case _: AppError.NotFound      => Status.NotFound
        case _: AppError.BadRequest    => Status.BadRequest
        case _: AppError.InternalError => Status.InternalServerError
      }

      val expectedCode = error match {
        case _: AppError.NotFound      => "NOT_FOUND"
        case _: AppError.BadRequest    => "BAD_REQUEST"
        case _: AppError.InternalError => "INTERNAL_ERROR"
      }

      val bodyText   = response.bodyText.compile.string.unsafeRunSync()
      val jsonResult = decode[ErrorResponse](bodyText)

      jsonResult.isRight shouldBe true
      jsonResult.foreach { errorResponse =>
        errorResponse.code shouldBe expectedCode
        errorResponse.message shouldBe error.message
      }
      response.status shouldBe expectedStatus
    }

  it should "handle Throwables with InternalServerError status and structure in response" in
    forAll(throwableGen) { error =>
      val response   = handler.handleError(error.getMessage)(error).unsafeRunSync()
      val bodyText   = response.bodyText.compile.string.unsafeRunSync()
      val jsonResult = decode[ErrorResponse](bodyText)

      jsonResult.isRight shouldBe true
      jsonResult.foreach { errorResponse =>
        errorResponse.code shouldBe "INTERNAL_ERROR"
        errorResponse.message shouldBe error.getMessage
      }
      response.status shouldBe Status.InternalServerError
    }
}
