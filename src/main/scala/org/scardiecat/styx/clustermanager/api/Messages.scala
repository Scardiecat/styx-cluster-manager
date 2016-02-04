package org.scardiecat.styx.clustermanager.api

import akka.cluster.{MemberStatus, Member}

import scala.collection.mutable.ArrayBuffer


sealed trait RequestMessage
case class ClusterStatus()

sealed trait ResponseMessage

case class GenericAck() extends ResponseMessage

case class ClusterMemberAddress (protocol: String, system: String, host: Option[String], port: Option[Int])

case class ClusterMemberUniqueAddress(address: ClusterMemberAddress, uid: Int)

case class ClusterMember(uniqueAddress: ClusterMemberUniqueAddress, memberStatus: String, roles: Set[String])

case class ClusterStatusResponse(members:List[ClusterMember])

case class LeaveMember(uniqueAddress: ClusterMemberUniqueAddress)

case class DownMember(uniqueAddress: ClusterMemberUniqueAddress)
