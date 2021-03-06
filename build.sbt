
name := """ss-toko-ijah"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += jdbc
libraryDependencies +=  "org.xerial" % "sqlite-jdbc" % "3.23.1"

libraryDependencies += "joda-time" % "joda-time" % "2.9.9"
libraryDependencies += "com.norbitltd" %% "spoiwo" % "1.3.0"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"