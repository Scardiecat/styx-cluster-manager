import NativePackagerHelper._


val commonSettings = Seq(
  organization := "org.scardiecat",
  version := "0.0.1",
  scalaVersion := "2.11.7",

  // build info
  buildInfoPackage := "meta",
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoOptions += BuildInfoOption.BuildTime,
  buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion
  ),
  publishMavenStyle := true
)

val dockerSettings = Seq(
  dockerExposedPorts := Seq(2551),
  maintainer in Docker := "Ralf Mueller <docker@scardiecat.org>"
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
  .settings(
    name := """styx-cluster-manager""",
    libraryDependencies ++= Dependencies.common,
    commonSettings,
    dockerSettings
  )
