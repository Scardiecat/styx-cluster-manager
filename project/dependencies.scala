import sbt._

object Dependencies {

  object Version {
    val akka = "2.4.1"
    val http = "2.0.3"
  }

  val common = Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
    "com.typesafe.akka" %% "akka-stream-experimental" % Version.http,
    "com.typesafe.akka" %% "akka-http-core-experimental" % Version.http,
    "com.typesafe.akka" %% "akka-http-experimental" % Version.http,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % Version.http,
    "org.scardiecat" %% "styx-utils" % "0.0.4",
    "org.scardiecat" %% "styx-akka-guice" % "0.0.2"
  )
}
