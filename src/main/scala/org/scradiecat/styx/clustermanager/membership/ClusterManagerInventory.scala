package org.scradiecat.styx.clustermanager.membership

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.cluster.{Member, Cluster}
import akka.cluster.ClusterEvent._
import org.scardiecat.styx.clustermanager.api._

import scala.collection.mutable.ArrayBuffer

class ClusterManagerInventory  extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  val inventory = collection.mutable.Map[akka.cluster.UniqueAddress, Member]()

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    /**
      * Member status changed to Joining.
      */
    case MemberJoined(member) =>
      log.info("Member Joined: {}", member.address)
      inventory(member.uniqueAddress)=  member
    /**
      * Member status changed to WeaklyUp.
      * A joining member can be moved to `WeaklyUp` if convergence
      * cannot be reached, i.e. there are unreachable nodes.
      * It will be moved to `Up` when convergence is reached.
      */
    case MemberWeaklyUp(member) =>
      log.info("Member is WeaklyUp: {}", member.address)
      inventory(member.uniqueAddress)=  member
    /**
      * Member status changed to Up.
      */
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      inventory(member.uniqueAddress)=  member
    /**
      * A member is considered as unreachable by the failure detector.
      */
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
      inventory(member.uniqueAddress)=  member
    /**
      * A member is considered as reachable by the failure detector
      * after having been unreachable.
      * @see [[UnreachableMember]]
      */
    case UnreachableMember(member) =>
      log.info("Member detected as reachable: {}", member)
      inventory(member.uniqueAddress)=  member
    /**
      * Member status changed to Leaving.
      */
    case MemberLeft(member) =>
      log.info("Member left: {}", member)
      inventory(member.uniqueAddress)=  member
    /**
      * Member status changed to `MemberStatus.Exiting` and will be removed
      * when all members have seen the `Exiting` status.
      */
    case MemberExited(member) =>
      inventory(member.uniqueAddress)=  member
      log.info("Member exited: {}", member);
    /**
      * Member completely removed from the cluster.
      * When `previousStatus` is `MemberStatus.Down` the node was removed
      * after being detected as unreachable and downed.
      * When `previousStatus` is `MemberStatus.Exiting` the node was removed
      * after graceful leaving and exiting.
      */
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
      inventory -= member.uniqueAddress
    case _: MemberEvent => // ignore

    case ClusterStatus =>
      log.info(inventory.toString())
      var list = ArrayBuffer[ClusterMember]()
      inventory.foreach {
        case(key, value) =>
          list += ClusterMember(
            ClusterMemberUniqueAddress(
              ClusterMemberAddress(value.uniqueAddress.address.protocol
                , value.uniqueAddress.address.system
                , value.uniqueAddress.address.host
                , value.uniqueAddress.address.port
              )
              , value.uniqueAddress.uid
            )
            ,value.status.toString
            , value.roles
          )
      }
      val clusterList = ClusterStatusResponse(list.toList)
      sender().tell(clusterList, self)
  }
}


object ClusterManagerInventory {

  /**
    * Create the Actor
    */
  def props() = Props(new ClusterManagerInventory())
}
