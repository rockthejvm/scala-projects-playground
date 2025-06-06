lazy val scrape =
  project
    .in(file("."))
    .settings(
      name         := "scrape",
      version      := "0.1.0-SNAPSHOT",
      scalaVersion := "3.7.0",
      libraryDependencies ++=
        "org.jsoup"                 % "jsoup"                      % "1.20.1" ::
          "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0" ::
          "org.quartz-scheduler"    % "quartz"                     % "2.5.0" ::
          "org.quartz-scheduler"    % "quartz-jobs"                % "2.5.0" ::
          "com.sun.mail"            % "javax.mail"                 % "1.6.2" ::
          Nil
    )
