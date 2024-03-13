import smithy4s.codegen.Smithy4sCodegenPlugin

ThisBuild / organization := "cc.blackquill"
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / Compile / fork := true

lazy val root = (project in file("."))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    name := "alasa-sona",
    libraryDependencies ++= Seq(
      // "core" module - IO, IOApp, schedulers
      // This pulls in the kernel and std modules automatically.
      "org.typelevel" %% "cats-effect" % "3.3.12",
      // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
      "org.typelevel" %% "cats-effect-kernel" % "3.3.12",
      // standard "effect" library (Queues, Console, Random etc.)
      "org.typelevel" %% "cats-effect-std" % "3.3.12",
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion.value,
      "org.tpolecat" %% "skunk-core" % "0.6.3",
      "org.tpolecat" %% "skunk-circe" % "0.6.3",
      "org.spongepowered" % "configurate-core" % "4.1.2",
      "org.spongepowered" % "configurate-yaml" % "4.1.2",
      "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
    ),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-ember-server",
    ).map(_ % "0.23.24"),
  )
