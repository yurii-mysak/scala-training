package com.github.scala_training
package domain.model.api.post.car

import java.util.UUID

final case class CreateCarResponse(id: UUID, message: String)
