ThisBuild / scalaVersion := "3.7.0"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.rtjvm"

lazy val chatApp =
  project
    .in(file("."))
    .aggregate(app.js, app.jvm)

lazy val app =
  crossProject(JSPlatform, JVMPlatform)
    .in(file("."))
    .settings(
      name := "chat-app",
      libraryDependencies ++=
        "com.lihaoyi"   %%% "upickle"       % "4.1.0" ::
          "com.lihaoyi" %%% "scalatags"     % "0.12.0" ::
          "io.circe"    %%% "circe-core"    % "0.14.6" ::
          "io.circe"    %%% "circe-generic" % "0.14.6" ::
          "io.circe"    %%% "circe-parser"  % "0.14.6" ::
          Nil
    )
    .jsSettings(
      Compile / fastOptJS / artifactPath := baseDirectory.value / "static/main.js",
      scalaJSUseMainModuleInitializer    := true,
      libraryDependencies ++=
        "org.scala-js"  %%% "scalajs-dom" % "2.8.0" ::
          "com.lihaoyi" %%% "scalatags"   % "0.12.0" ::
          Nil
    )
    .jvmSettings(
      libraryDependencies ++=
        "com.lihaoyi"             %% "cask"                 % "0.10.2" ::
          "io.getquill"           %% "quill-jdbc"           % "4.8.6" ::
          "com.lihaoyi"           %% "scalasql"             % "0.1.19" ::
          "com.lihaoyi"           %% "os-lib"               % "0.11.4" ::
          "ch.vorburger.mariaDB4j" % "mariaDB4j"            % "3.2.0" ::
          "mysql"                  % "mysql-connector-java" % "8.0.33" ::
          "com.zaxxer"             % "HikariCP"             % "5.1.0" ::
          Nil
    )
