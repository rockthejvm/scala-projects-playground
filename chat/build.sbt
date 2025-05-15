ThisBuild / organization := "com.rtjvm"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"
ThisBuild / resolvers    += "Maven Repo" at "https://mvnrepository.com/artifact"
ThisBuild / libraryDependencies ++=
  "com.lihaoyi"   %% "requests"  % "0.9.0" ::
    "com.lihaoyi" %% "upickle"   % "4.1.0" ::
    "com.lihaoyi" %% "os-lib"    % "0.11.4" ::
    "com.lihaoyi" %% "scalatags" % "0.13.1" ::
    "com.lihaoyi" %% "cask"      % "0.10.2" ::
    "com.lihaoyi" %% "utest"     % "0.8.5" ::
    Nil

lazy val chat =
  project
    .in(file("."))
    .settings(name := "chat")
    .aggregate(backend, frontend, shared)

lazy val backend =
  project
    .settings(name := "backend")
    .dependsOn(shared)
    .settings(
      libraryDependencies ++=
        "io.getquill" %% "quill-jdbc" % "4.8.6" ::
          "com.lihaoyi" %% "os-lib" % "0.11.4" ::
          "ch.vorburger.mariaDB4j" % "mariaDB4j" % "3.2.0" ::
          "mysql" % "mysql-connector-java" % "8.0.33" ::
          "com.zaxxer" % "HikariCP" % "5.1.0" ::
          Nil
    )

lazy val frontend =
  project
    .settings(name := "frontend")
    .dependsOn(shared)

lazy val shared =
  project
    .settings(name := "shared")
