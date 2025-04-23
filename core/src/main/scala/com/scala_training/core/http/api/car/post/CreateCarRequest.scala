package com.scala_training.core.http.api.car.post

import java.time.Year

case class CreateCarRequest(make: String, model: String, year: Year)
