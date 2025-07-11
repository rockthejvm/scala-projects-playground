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
      fork := true,
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
        "com.lihaoyi"              %% "cask"              % "0.10.2" ::
          "com.lihaoyi"            %% "scalasql"          % "0.1.19" ::
          "com.lihaoyi"            %% "os-lib"            % "0.11.4" ::
          "com.zaxxer"              % "HikariCP"          % "5.1.0" ::
          "io.zonky.test"           % "embedded-postgres" % "2.1.0" ::
          "com.impossibl.pgjdbc-ng" % "pgjdbc-ng"         % "0.8.9" ::
          Nil
    )
