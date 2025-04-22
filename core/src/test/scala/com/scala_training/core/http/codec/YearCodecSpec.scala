package com.scala_training.core.http.codec

import io.circe.parser.*
import io.circe.syntax.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.scala_training.core.http.codec.YearCodec.*
import com.scala_training.core.http.codec.YearCodec.given

import java.time.Year

class YearCodecSpec extends AnyFlatSpec with Matchers {
  "YearCodec" should "encode Year correctly" in {
    val year = Year.of(2024)
    year.asJson.noSpaces should be("2024")
  }

  it should "decode Year correctly" in {
    val json = "2024"
    decode[Year](json) should be(Right(Year.of(2024)))
  }

  it should "maintain value through encoding and decoding" in {
    val original = Year.of(2024)
    val json     = original.asJson.noSpaces
    decode[Year](json) should be(Right(original))
  }

  it should "fail to decode invalid year values" in {
    val invalidJson = "\"invalid\""
    decode[Year](invalidJson).isLeft should be(true)
  }

  it should "handle minimum and maximum years" in {
    val minYear = Year.of(-999999999)
    val maxYear = Year.of(999999999)

    decode[Year](minYear.asJson.noSpaces) should be(Right(minYear))
    decode[Year](maxYear.asJson.noSpaces) should be(Right(maxYear))
  }
}
