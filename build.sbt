ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.3"

val akkaVersion = "2.10.0"
val akkaHttpVersion = "10.7.0"
val scalaTestVersion = "3.2.19"
val http4sVersion = "0.23.30"

lazy val root = (project in file("."))
  .settings(
    name := "scala-training",
    idePackagePrefix := Some("com.github.scala-training"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    scalacOptions += "-Wnonunit-statement",
    libraryDependencies ++= Seq(
      // akka streams
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      // akka http
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
      // testing
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion,

      // JWT
      "com.github.jwt-scala" %% "jwt-core" % "10.0.4",
      // cats
      "org.typelevel" %% "cats-effect" % "3.5.7",

      // http4s
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
    )
  )
