ThisBuild / version           := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion      := "2.13.16"
ThisBuild / scalafmtOnCompile := true

// Common versions
lazy val akkaVersion                     = "2.10.0"
lazy val akkaHttpVersion                 = "10.7.0"
lazy val scalaTestVersion                = "3.2.19"
lazy val http4sVersion                   = "0.22.15"
lazy val circeVersion                    = "0.13.0"
lazy val slf4jVersion                    = "2.0.17"
lazy val akkaPersistenceCassandraVersion = "1.3.0"
lazy val akkaHttpCirceVersion            = "1.34.0"
lazy val catsEffectVersion               = "2.5.5"
lazy val jwtScalaVersion                 = "10.0.4"
lazy val doobieVersion                   = "0.13.4"
lazy val scalaTestCatsEffectVersion      = "1.1.1"
lazy val testContainerVersion            = "1.20.6"
lazy val log4catsVersion                 = "1.3.1"

lazy val root = (project in file("."))
  .aggregate(core, akka_impl, cats_impl)
  .settings(
    name := "scala-training"
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "io.circe"          %% "circe-core"                 % circeVersion,
      "io.circe"          %% "circe-generic"              % circeVersion,
      "io.circe"          %% "circe-parser"               % circeVersion,
      "io.circe"          %% "circe-generic-extras"       % circeVersion,
      "org.scalatest"     %% "scalatest"                  % scalaTestVersion % "test",
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      // todo: not ideal, since we use cats and akka in core
      "org.tpolecat"      %% "doobie-core"                % doobieVersion,
      "org.tpolecat"      %% "doobie-postgres"            % doobieVersion
    )
  )

lazy val akka_impl = (project in file("akka_impl"))
  .dependsOn(core)
  .settings(
    name                := "akka_impl",
    Compile / mainClass := Some("com.training.akka_impl.Application"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-effect"                % catsEffectVersion,
      "com.typesafe.akka" %% "akka-stream"                % akkaVersion,
      "com.typesafe.akka" %% "akka-http"                  % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit"          % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe"            % akkaHttpCirceVersion,
      "com.typesafe.akka" %% "akka-testkit"               % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools"         % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence"           % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed"     % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query"     % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-cassandra" % akkaPersistenceCassandraVersion,
      "org.scalatest"     %% "scalatest"                  % scalaTestVersion % "test"
    )
  )

lazy val cats_impl = (project in file("cats_impl"))
  .dependsOn(core)
  .settings(
    Compile / mainClass := Some("com.training.cats_impl.Application"),
    name                := "cats_impl",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps",
      "-language:higherKinds"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"         % catsEffectVersion withSources () withJavadoc (),
      "org.http4s"            %% "http4s-ember-server" % http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
      "com.github.jwt-scala"  %% "jwt-core"            % jwtScalaVersion,
      "org.http4s"            %% "http4s-circe"        % http4sVersion,
      "io.circe"              %% "circe-fs2"           % circeVersion,
      "org.tpolecat"          %% "doobie-core"         % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"       % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"     % doobieVersion,
      "org.postgresql"         % "postgresql"          % "42.7.2", // postgres driver
      "com.github.pureconfig" %% "pureconfig"          % "0.17.6",
      "org.flywaydb"           % "flyway-core"         % "9.22.3"
    )
  )
