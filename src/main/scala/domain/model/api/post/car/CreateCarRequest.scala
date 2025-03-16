package com.github.scala_training
package domain.model.api.post.car

import java.util.UUID

final case class CreateCarRequest(make: String, model: String, year: Int)
