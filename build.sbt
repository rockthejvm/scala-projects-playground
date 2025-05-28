name         := "scala-projects-playground"
version      := "0.1"
scalaVersion := "3.7.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests"  % "0.9.0",
  "com.lihaoyi" %% "upickle"   % "4.1.0",
  "com.lihaoyi" %% "os-lib"    % "0.11.4",
  "com.lihaoyi" %% "scalatags" % "0.13.1",
  "com.lihaoyi" %% "cask"      % "0.10.2",
  // Java libraries
  // scraping
  "org.jsoup" % "jsoup" % "1.20.1",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  // markdown
  "org.commonmark" % "commonmark" % "0.24.0",
  // http apis
  "org.asynchttpclient" % "async-http-client" % "3.0.2",
  // mandelbrot
  "org.openjfx" % "javafx-base"     % "24.0.1",
  "org.openjfx" % "javafx-controls" % "24.0.1",
  "org.openjfx" % "javafx-fxml"     % "24.0.1",
  "org.openjfx" % "javafx-swing"    % "24.0.1"
)

// blog build pipeline

// Custom tasks
lazy val blogResourceDir = settingKey[File]("Directory containing blog resources")
lazy val buildBlog       = taskKey[Unit]("Build the blog")
lazy val runBlog         = taskKey[Unit]("Run the blog on a local server")
lazy val watchBlog       = taskKey[Unit]("Watch for changes and rebuild blog")

// Set resource directory
blogResourceDir := (Compile / resourceDirectory).value

// Build blog task
buildBlog := {
  (Compile / runMain).toTask(" blog.BlogExpanded buildBlog").value
}

// Watch and reload task
watchBlog := {
  val log         = streams.value.log
  val resourceDir = blogResourceDir.value
  val markdownDir = resourceDir / "blog"
  val state       = Keys.state.value // Get the current state

  log.info(s"Watching for changes in $markdownDir")

  // Initial build
  buildBlog.value

  // Setup file watcher
  FileWatcher.watch(markdownDir, buildBlog, state, log)
}

// Run blog task with auto-reload
runBlog := {
  val log = streams.value.log

  // Start watcher in a separate thread
  val watcherThread = new Thread {
    override def run(): Unit = {
      watchBlog.value
    }
  }
  watcherThread.setDaemon(true)
  watcherThread.start()

  log.info("Starting blog server...")
  (Compile / runMain).toTask(" blog.RunServer").value
}
