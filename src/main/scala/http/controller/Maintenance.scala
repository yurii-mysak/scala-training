package http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import domain.adt.MaintenanceStatus
import domain.model.api.maintenance.{CreateMaintenanceRequest, CreateMaintenanceResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import java.util.UUID

object Maintenance extends FailFastCirceSupport {
  import io.circe.generic.auto._

  def routes(implicit ec: ExecutionContext): Route = {
    path("maintenance") {
      post {
        entity(as[CreateMaintenanceRequest]) { request =>
          val responseFuture = Future {
            val id = UUID.randomUUID()
            CreateMaintenanceResponse(id, MaintenanceStatus.Created)
          }

          onComplete(responseFuture) {
            case Success(response) => complete(response)
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
  }
}