package org.scardiecat.styx.clustermanager.service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.CacheDirectives.{`proxy-revalidate`, `no-cache`}
import akka.http.scaladsl.model.headers.`Cache-Control`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ClusterManagerService {


  implicit val timeout = Timeout(5 seconds)


  val exceptionHandler = ExceptionHandler {
    case ex: GenericException => complete(ex.errorCode, ex.msg)
  }

  def ClusterRoute()(implicit ec: ExecutionContext, system: ActorSystem) =
    handleExceptions(exceptionHandler) {
      path("version") {
        respondWithHeader(`Cache-Control`(`proxy-revalidate`, `no-cache`)) {
          get {
            complete(meta.BuildInfo.toJson)
          }
        }
      }
    }
}