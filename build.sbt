ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.3"

lazy val akkaVersion = "2.10.0"
lazy val akkaHttpVersion = "10.7.0"
lazy val scalaTestVersion = "3.2.19"
lazy val http4sVersion = "0.23.30"
lazy val cassandraVersion = "1.3.0"
lazy val json4sVersion = "4.0.7"
lazy val slickVersion = "3.5.2"
lazy val slf4jVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "scala-training",
    idePackagePrefix := Some("com.github.scala_training"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    scalacOptions += "-Wnonunit-statement",
    scalacOptions += " -target:17",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    libraryDependencies ++= Seq(
      // akka streams
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      // akka http
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
      // akka persistence
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.lightbend.akka" %% "akka-persistence-jdbc" % "5.5.0",
      // testing
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion,

      // JWT
      "com.github.jwt-scala" %% "jwt-core" % "10.0.4",
      // cats
      "org.typelevel" %% "cats-effect" % "3.5.7",

      // logging
      "org.slf4j"              % "slf4j-simple"        % slf4jVersion,
      // http4s
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,

      "org.json4s" %% "json4s-native" % json4sVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" % "akka-persistence-cassandra_3" % cassandraVersion
    )
  )
