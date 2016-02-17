package org.scradiecat.styx.clustermanager.membership

import akka.actor.{Address, Props, ActorLogging, Actor}
import akka.cluster.Cluster
import org.scardiecat.styx.clustermanager.api.{ClusterMemberUniqueAddress, LeaveMember, DownMember}


class MemberManagement  extends Actor with ActorLogging {

  val cluster = Cluster(context.system)


  override def preStart(): Unit = {
  }

  override def postStop(): Unit = {

  }

  def receive = {
    case LeaveMember(memberUniqueAdress) => leaveMember(memberUniqueAdress)
    case DownMember(memberUniqueAdress) => downMember(memberUniqueAdress)
    case _      => log.info("received unknown message")
  }

  def leaveMember(leaveMember: ClusterMemberUniqueAddress) = {
    cluster.leave(new Address(leaveMember.address.protocol
      , leaveMember.address.system
      , leaveMember.address.host.getOrElse("")
      , leaveMember.address.port.getOrElse(0)))
  }

  def downMember(downMember: ClusterMemberUniqueAddress) = {
    cluster.down(new Address(downMember.address.protocol
      , downMember.address.system
      , downMember.address.host.getOrElse("")
      , downMember.address.port.getOrElse(0)))
  }
}


object MemberManagement {

  /**
    * Create the Actor
    */
  def props() = Props(new MemberManagement())
}
