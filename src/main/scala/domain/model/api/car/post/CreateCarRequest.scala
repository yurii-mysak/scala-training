package domain.model.api.car.post

import java.time.Year

case class CreateCarRequest(make: String, model: String, year: Year)