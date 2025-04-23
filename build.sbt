ThisBuild / version           := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion      := "3.6.4"
ThisBuild / scalafmtOnCompile := true

// Common versions
lazy val akkaVersion                     = "2.10.0"
lazy val akkaHttpVersion                 = "10.7.0"
lazy val scalaTestVersion                = "3.2.19"
lazy val slf4jVersion                    = "2.0.17"
lazy val akkaPersistenceCassandraVersion = "1.3.0"
lazy val akkaHttpCirceVersion            = "1.40.0-RC3"
lazy val scalaTestCatsEffectVersion      = "1.1.1"
lazy val testContainerVersion            = "1.20.6"
lazy val http4sVersion                   = "1.0.0-M40" // for Cats Effect 3
lazy val doobieVersion                   = "1.0.0-RC9" // for Cats Effect 3
lazy val circeVersion                    = "0.14.12"
lazy val circeFs2Version                 = "0.14.1"
lazy val circeGenericExtrasVersion       = "0.14.4"
lazy val catsEffectVersion               = "3.6.1"
lazy val log4catsVersion                 = "2.7.0"
lazy val pureConfigVersion               = "0.17.6"    // Scala 3 compatible
lazy val jwtScalaVersion                 = "10.0.4"

lazy val root = (project in file("."))
  .aggregate(core, akka_impl, cats_impl)
  .settings(
    name := "scala-training",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps",
      "-language:higherKinds",
      "-source:3.3",
      "-new-syntax",
      "-indent",
      "-explain"
    )
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      // Scala 3 dependencies
      "io.circe"          %% "circe-core"                 % circeVersion,
      "io.circe"          %% "circe-generic"              % circeVersion,
      "io.circe"          %% "circe-parser"               % circeVersion,
      "org.typelevel"     %% "cats-core"                  % "2.13.0",
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "org.tpolecat"      %% "doobie-core"                % doobieVersion,
      "org.tpolecat"      %% "doobie-postgres"            % doobieVersion,
      "org.scalatest"     %% "scalatest"                  % scalaTestVersion % Test,
      "org.scalacheck"    %% "scalacheck"                 % "1.18.1"         % Test,

      // Scala 2.13 dependency with exclusions
      ("io.circe" %% "circe-generic-extras" % circeGenericExtrasVersion)
        .cross(CrossVersion.for3Use2_13)
        .exclude("io.circe", "circe-core_2.13")
        .exclude("io.circe", "circe-generic_2.13")
        .exclude("org.typelevel", "cats-core_2.13")
    )
  )

lazy val akka_impl = (project in file("akka_impl"))
  .dependsOn(core)
  .settings(
    name                := "akka_impl",
    Compile / mainClass := Some("com.scala_training.akka_impl.Application"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.typelevel"      %% "cats-effect"                % catsEffectVersion,
      "com.typesafe.akka"  %% "akka-stream"                % akkaVersion,
      "com.typesafe.akka"  %% "akka-http"                  % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-testkit"          % akkaHttpVersion,
      ("de.heikoseeberger" %% "akka-http-circe"            % akkaHttpCirceVersion)
        .cross(CrossVersion.for3Use2_13)
        .exclude(
          "io.circe",
          "circe-core_2.13"
        )
        .exclude(
          "io.circe",
          "circe-generic_2.13"
        )
        .exclude(
          "io.circe",
          "circe-parser_2.13"
        )
        .exclude("com.typesafe.akka", "akka-http_2.13")
        .exclude("com.typesafe.akka", "akka-http-core_2.13")
        .exclude("com.typesafe.akka", "akka-http_2.13")
        .exclude("com.typesafe.akka", "akka-http_2.13"),
      "com.typesafe.akka"  %% "akka-testkit"               % akkaVersion,
      "com.typesafe.akka"  %% "akka-cluster-tools"         % akkaVersion,
      "com.typesafe.akka"  %% "akka-persistence"           % akkaVersion,
      "com.typesafe.akka"  %% "akka-persistence-typed"     % akkaVersion,
      "com.typesafe.akka"  %% "akka-persistence-query"     % akkaVersion,
      "com.typesafe.akka"  %% "akka-persistence-cassandra" % akkaPersistenceCassandraVersion,
      "org.scalatest"      %% "scalatest"                  % scalaTestVersion % Test
    )
  )

lazy val cats_impl = (project in file("cats_impl"))
  .dependsOn(core)
  .settings(
    Compile / mainClass := Some("com.scala_training.cats_impl.Application"),
    name                := "cats_impl",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"                   % catsEffectVersion withSources () withJavadoc (),
      "org.typelevel"         %% "log4cats-slf4j"                % log4catsVersion,
      "ch.qos.logback"         % "logback-classic"               % "1.5.18",
      "org.http4s"            %% "http4s-ember-server"           % http4sVersion,
      "org.http4s"            %% "http4s-dsl"                    % http4sVersion,
      "com.github.jwt-scala"  %% "jwt-core"                      % jwtScalaVersion,
      "org.http4s"            %% "http4s-circe"                  % http4sVersion,
      "io.circe"              %% "circe-fs2"                     % circeFs2Version,
      "org.tpolecat"          %% "doobie-core"                   % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"                 % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"               % doobieVersion,
//      "org.postgresql"         % "postgresql"                    % "42.7.5", // postgres driver
      "com.github.pureconfig" %% "pureconfig-core"               % "0.17.8",
      "com.github.pureconfig" %% "pureconfig-cats-effect"        % "0.17.8",
      "org.flywaydb"           % "flyway-core"                   % "11.7.2",
      "org.flywaydb"           % "flyway-database-postgresql"    % "11.7.2",
      "org.scalatest"         %% "scalatest-flatspec"            % scalaTestVersion % Test,
      "org.scalatest"         %% "scalatest-matchers-core"       % scalaTestVersion % Test,
      "org.scalatest"         %% "scalatest-shouldmatchers"      % scalaTestVersion % Test,
      "org.scalatestplus"     %% "scalacheck-1-18"               % "3.2.19.0"       % Test,
      "org.typelevel"         %% "cats-effect-testing-scalatest" % "1.6.0"          % Test
    )
  )
