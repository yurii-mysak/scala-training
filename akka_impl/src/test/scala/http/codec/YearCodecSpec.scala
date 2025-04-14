package http.codec

import io.circe.parser._
import io.circe.syntax._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Year

class YearCodecSpec extends AnyFlatSpec with Matchers {
  import YearCodec._

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
