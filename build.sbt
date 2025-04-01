ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val akkaVersion = "2.10.0"
lazy val akkaHttpVersion = "10.7.0"
lazy val scalaTestVersion = "3.2.19"
lazy val http4sVersion = "0.23.30"
lazy val cassandraVersion = "1.3.0"
lazy val json4sVersion = "4.0.7"
lazy val slickVersion = "3.5.2"
lazy val slf4jVersion = "2.0.0"
lazy val circeVersion = "0.13.0" // todo: how can we update it and keep  akkaHttpJsonSerializersVersion?
lazy val akkaHttpJsonSerializersVersion = "1.34.0"

lazy val root = (project in file("."))
  .settings(
    name := "scala-training",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    scalacOptions += "-Wnonunit-statement",
    scalacOptions += " -target:17",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    libraryDependencies ++= Seq(
      // akka streams
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      // akka http
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,

      // json
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      // akka-http-circe (part of Akka ecosystem)
      "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJsonSerializersVersion,

      // akka persistence
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.lightbend.akka" %% "akka-persistence-jdbc" % "5.5.0",

      // testing (only Akka testkit needs cross-version)
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion,

      // JWT (native Scala 3 support)
      "com.github.jwt-scala" %% "jwt-core" % "10.0.4",
      // cats (native Scala 3 support)
      "org.typelevel" %% "cats-effect" % "3.5.7",

      // logging
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      // http4s (native Scala 3 support)
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,

      // More Akka dependencies
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion
    )
  )
