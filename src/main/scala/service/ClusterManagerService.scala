package org.scardiecat.styx.clustermanager.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import akka.http.scaladsl.model.headers.CacheDirectives.{`proxy-revalidate`, `no-cache`}
import akka.http.scaladsl.model.headers.`Cache-Control`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directives, ExceptionHandler}
import akka.util.Timeout
import org.scardiecat.styx.clustermanager.api._
import org.scradiecat.styx.clustermanager.membership.{MemberManagement, ClusterManagerInventory}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import spray.json._
import akka.pattern.ask
import scala.concurrent.Future

trait JsonSupport extends  SprayJsonSupport with DefaultJsonProtocol {
  implicit val clusterMemberAdress = jsonFormat4(ClusterMemberAddress)
  implicit val clusterMemberUniqueAdress = jsonFormat2(ClusterMemberUniqueAddress)
  implicit val clusterMember = jsonFormat3(ClusterMember)
  implicit val clusterStatusResponse = jsonFormat1(ClusterStatusResponse)
  implicit val leaveMember = jsonFormat1(LeaveMember)
  implicit val downMember = jsonFormat1(DownMember)
}

class RequestHelper (monitorActor:ActorRef, managerActor:ActorRef)(implicit timeout:Timeout) {

  def status(): Future[Any] =
    monitorActor ? ClusterStatus

  def leave(leaveMember: LeaveMember) : Future[Any] =
    managerActor ? leaveMember

  def down(downMember: DownMember) : Future[Any] =
    managerActor ? downMember
}
object ClusterManagerService  extends JsonSupport{


  implicit val timeout = Timeout(5 seconds)


  val exceptionHandler = ExceptionHandler {
    case ex: GenericException => complete(ex.errorCode, ex.msg)
  }

  def ClusterRoute(monitorActor:ActorRef, managerActor:ActorRef)(implicit ec: ExecutionContext, system: ActorSystem) =
    handleExceptions(exceptionHandler) {
      val route = new RequestHelper(monitorActor, managerActor)(timeout)
      path("version") {
        respondWithHeader(`Cache-Control`(`proxy-revalidate`, `no-cache`)) {
          get {
            complete(meta.BuildInfo.toJson)
          }
        }
      }~
      path("status") {
        respondWithHeader(`Cache-Control`(`proxy-revalidate`, `no-cache`)) {
          get {
            complete {
              route.status().map(_ match {
                case message: ClusterStatusResponse => message
              })
            }
          }
        }
      }~
      path("member" / "leave"){
        post {
          entity(as[LeaveMember]) { leaveMessage =>
            route.leave(leaveMessage)
            complete("Done")
          }
        }
      }~
        path("member" / "down"){
          post {
            entity(as[DownMember]) { downMessage =>
              route.down(downMessage)
              complete("Done")
            }
          }
        }
    }
}