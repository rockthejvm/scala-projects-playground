// project/FileWatcher.scala
import sbt.*

import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*

object FileWatcher {
  def watch(dir: File, action: TaskKey[Unit], state: State, log: Logger): Unit = {
    val path = dir.toPath
    val watchService = FileSystems.getDefault.newWatchService()
    path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

    log.info(s"Watching directory: ${dir.getAbsolutePath}")

    try {
      while (true) {
        val watchKey = watchService.take()
        val events = watchKey.pollEvents()

        if (!events.isEmpty) {
          val fileChanged = events.get(0).context().toString
          if (fileChanged.endsWith(".md")) {
            log.info(s"Detected change in: $fileChanged")
            Thread.sleep(500) // Small delay to ensure file is fully written
            val extracted = Project.extract(state)
            extracted.runTask(action, state)
            log.info("Blog rebuilt successfully")
          }
        }

        if (!watchKey.reset()) {
          log.error("Watch key no longer valid")
          return
        }
      }
    } catch {
      case e: Exception =>
        log.error(s"Error watching directory: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      watchService.close()
    }
  }
}