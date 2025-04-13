package http.codec

import io.circe.{Decoder, Encoder}

import java.time.Year

object YearCodec {
  // Custom Year encoder/decoder
  implicit val yearEncoder: Encoder[Year] = Encoder.encodeInt.contramap[Year](_.getValue)
  implicit val yearDecoder: Decoder[Year] = Decoder.decodeInt.map(Year.of)
}