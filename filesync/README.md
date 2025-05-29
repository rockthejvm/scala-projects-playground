# File Sync

NOTE: Sometimes, this app throws error when running from IntelliJ. This is because the `agent` is bundled as a resource in the `Sync` project, which requires running `sbt assembly` at least once. So run `sbt assembly` in the terminal before running the app from IntelliJ.

Also, when running the app from IntelliJ, configure the run configuration for `sync.Sync` to pass program arguments (source and destination paths).

## Exercises

- Syncing folders/sub-folders

  Track `Rpc.CreateFolder` case class

- Syncing deleted files/folders 
  
  Track `Rpc.DeletePath` case class

