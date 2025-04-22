package com.scala_training.core.http.codec

import io.circe.{Decoder, Encoder}

import java.time.Year

object YearCodec {
  // Custom Year encoder/decoder
  given Encoder[Year] = Encoder.encodeInt.contramap[Year](_.getValue)
  given Decoder[Year] = Decoder.decodeInt.map(Year.of)
}
