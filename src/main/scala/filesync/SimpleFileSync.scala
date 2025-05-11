package filesync

object SimpleFileSync {

  /** There are three cases the above code does not support:
    *   - If the source path is empty and the destination path contains a folder. This is a "delete" which we will
    *     ignore for now; supporting this is left as an exercise for the reader
    *   - If the source path is a folder and the destination path is a folder. In this case doing nothing is fine:
    *     os.walk will enter the source path folder and process all the files within it recursively
    */
  def sync(src: os.Path, dest: os.Path): Unit = {
    os.walk(src).foreach { srcSubPath =>
      val subPath     = srcSubPath.subRelativeTo(src)
      val destSubPath = dest / subPath

      (os.isDir(srcSubPath), os.isDir(destSubPath)) match {
        case (false, true) | (true, false) =>
          os.copy.over(srcSubPath, destSubPath, createFolders = true)
        case (false, false)
            if !os.exists(destSubPath)
              || !os.read.bytes(srcSubPath).sameElements(os.read.bytes(destSubPath)) =>
          os.copy.over(srcSubPath, destSubPath, createFolders = true)
        case _ => // do nothing
      }
    }
  }

  @main
  def main(): Unit = {
    sync(os.pwd / "src/main/scala", os.home / "Temp" / "blah")
  }
}
