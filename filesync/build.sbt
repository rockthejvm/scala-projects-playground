import java.nio.file.Paths

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"

ThisBuild / libraryDependencies ++=
  "com.lihaoyi"   %% "upickle"      % "4.1.0" ::
    "com.lihaoyi" %% "os-lib"       % "0.11.4" ::
    "com.lihaoyi" %% "os-lib-watch" % "0.11.4" ::
    "com.lihaoyi" %% "castor"       % "0.3.0" ::
    Nil

lazy val shared = project

lazy val agent =
  project
    .dependsOn(shared)
    .settings(
      assembly / mainClass       := Some("sync.Agent"),
      assembly / assemblyJarName := "agent.jar"
    )

lazy val sync =
  project
    .dependsOn(shared, agent)
    .settings(
      Compile / resources := (Compile / resources).value ++ Seq(
        {
          val agentJar = (agent / assembly / assemblyOutputPath).value
          println(s"************************************** $agentJar")
          /*val destPath = target.value / "agent2.jar"
          IO.copyFile(agentJar, destPath)*/
          file(agentJar.getAbsolutePath)
        }
      )
    )
