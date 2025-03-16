//package com.github.scala_training
//package persistence.repository
//
//import persistence.model.car.Car
//
//import java.util.UUID
//import scala.concurrent.{ExecutionContext, Future}
//
//class MaintenanceRepository()(implicit ec: ExecutionContext) {
//  def getAll: Future[Seq[Car]] = Future {
//
//  }
//
//  def getById(id: UUID): Future[Option[Car]] = Future {
//    cars.get(id)
//  }
//
//  def create(car: Car): Future[Car] = Future {
//    cars += (car.id -> car)
//    car
//  }
//}