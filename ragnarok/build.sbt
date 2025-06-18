lazy val ragnarok =
  project
    .in(file("."))
    .settings(
      name         := "ragnarok",
      version      := "0.1.0-SNAPSHOT",
      scalaVersion := "3.7.0",
      libraryDependencies ++=
        Circe.All ++
          Http4s.All ++
          Langchain4j.All ++
          Logging.All ++
          (
            "com.lihaoyi"   %% "cask"    % "0.10.2" ::
              "com.lihaoyi" %% "upickle" % "4.2.1" ::
              "com.lihaoyi" %% "os-lib"  % "0.11.4" ::
              Nil
          ),
      run / fork           := true,
      run / connectInput   := true,
      run / outputStrategy := Some(StdoutOutput),
      run / envVars := Map(
        "OPENAI_API_KEY" -> System.getenv("OPENAI_API_KEY"),
        "LOG_LEVEL"      -> "DEBUG"
      ),
      run / javaOptions ++= Seq(
        "-Dlogback.configurationFile=logback.xml",
        "-Xmx2G",
        "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug"
      ),
      scalacOptions ++= Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-Wconf:cat=deprecation:warning" // Make deprecation warnings non-fatal
      ),
      Compile / mainClass := Some("ragnorok.CaskServer"),
      addCommandAlias("runApp", "ragnarok/runMain ragnorok.Main"),

      // Assembly settings for creating a fat JAR
      assembly / mainClass       := Some("ragnorok.Main"),
      assembly / assemblyJarName := "ragnarok.jar",
      assembly / assemblyMergeStrategy := {
        case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
        case PathList("META-INF", _*)            => MergeStrategy.first
        case "module-info.class"                 => MergeStrategy.discard
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      }
    )

/*// Disable parallel execution in SBT
Global / concurrentRestrictions := Seq(
  Tags.limit(Tags.CPU, 1),
  Tags.limit(Tags.Network, 1)
)*/
