package sync

import java.io.{DataInputStream, DataOutputStream}

object Agent {
  def main(args: Array[String]): Unit = {
    val input  = new DataInputStream(System.in)
    val output = new DataOutputStream(System.out)

    while (true) try {
      val rpc = Shared.receive[Rpc](input)
      System.err.println("Agent handling: " + rpc)

      rpc match {
        case Rpc.StatPath(path) =>
          Shared.send(output, Rpc.StatInfo(path, Shared.hashPath(os.pwd / path)))
        case Rpc.WriteOver(bytes, path) =>
          os.remove.all(os.pwd / path)
          os.write.over(os.pwd / path, bytes, createFolders = true)
        case Rpc.DeletePath(path) =>
          os.remove.all(os.pwd / path)
        case sync.Rpc.CreateFolder(path) =>
          os.makeDir.all(os.pwd / path)
      }
    } catch {
      case e: java.io.EOFException =>
        System.err.println("+--------------------------------------------------+")
        e.printStackTrace(System.err)
        System.err.println("+--------------------------------------------------+")
        System.exit(0)
    }
  }
}
