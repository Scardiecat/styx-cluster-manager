package org.scardiecat.styx.clustermanager.main

import akka.actor.ActorSystem
import com.google.inject.{Guice, Injector}
import com.typesafe.config.{ConfigFactory, Config}
import org.scardiecat.styx.DockerAkkaUtils
import org.scardiecat.styx.akkaguice.AkkaModule
import org.scardiecat.styx.clustermanager.di.ConfigModule
import org.scardiecat.styx.clustermanager.service.ClusterManagerService
import org.scardiecat.styx.utils.commandline.CommandlineParser
import net.codingwell.scalaguice.InjectorExtensions._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object ClusterManagerBoot extends App {

  val fallbackConfig:Config = ConfigFactory.load()
  var commandline = CommandlineParser.parse(args,meta.BuildInfo.name+":"+meta.BuildInfo.version, fallbackConfig, Seq[String]("cluster-manager"))
  val hostAddress: String = java.net.InetAddress.getLocalHost.getHostAddress
  lazy val actorSystemName:String = commandline.actorSystemName
  val config: Config = DockerAkkaUtils.dockerAkkaConfigProvider(fallbackConfig,hostAddress,commandline)

  val confModule = new ConfigModule(config, actorSystemName)
  val akkaModule = new AkkaModule()

  val injector: Injector = Guice.createInjector(confModule,akkaModule)

  implicit val system = injector.instance[ActorSystem]
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()


//
//  override val config = ConfigFactory.load()
//  override val logger = Logging(system, getClass)

  Http().bindAndHandle(ClusterManagerService.ClusterRoute, config.getString("http.interface"), config.getInt("http.port"))

}
