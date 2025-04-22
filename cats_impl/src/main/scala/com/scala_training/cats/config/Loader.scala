package com.scala_training.cats.config

import cats.effect.Sync
import pureconfig.*

object Loader {

  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay(ConfigSource.default.loadOrThrow[AppConfig])
}
