name := """PlayErrorHandler"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

//libraryDependencies += guice
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % "test",
  "org.mockito" % "mockito-core" % "1.8.5" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.h2database" % "h2" % "1.4.188",
  "org.mindrot" % "jbcrypt" % "0.4",
  specs2 % Test,
  cache,
  evolutions

)

javaOptions in Test += "-Dconfig.file=conf/test.conf"
