ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

lazy val root = (project in file("."))
  .settings(
    name := "musical-chairs",
    idePackagePrefix := Some("org.musical.chairs"),
  )

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client4" %% "circe" % "4.0.0-M16",
  "com.lihaoyi" %% "upickle" % "4.0.0",
  "javazoom" % "jlayer" % "1.0.1"
)
