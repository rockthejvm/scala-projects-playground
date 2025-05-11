package sync

import castor.Context.Simple.global

object Sync:
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("Usage: sync <src> <dest>")
      System.exit(1)
    }

    val src  = os.Path(args(0))
    val dest = os.Path(args(1))

    if (!os.exists(src)) {
      println(s"Source path does not exist: $src")
      System.exit(2)
    }

    println(s"Syncing $src to $dest ...")

    val agentExecutable = os.temp(os.read.bytes(os.resource / "agent.jar"))
    os.perms.set(agentExecutable, "rwxr-xr-x")
    val agent = os.proc("java", "-jar", agentExecutable).spawn(cwd = dest)

    object SyncActor extends castor.SimpleActor[Msg]:
      def run(msg: Msg): Unit = {
        println("SyncActor handling: " + msg)
        msg match {
          case ChangedPath(value, false) =>
            Shared.send(agent.stdin.data, Rpc.StatPath(value))
          case ChangedPath(value, true) =>
            Shared.send(agent.stdin.data, Rpc.DeletePath(value))
          case AgentResponse(Rpc.StatInfo(p, remoteHash)) =>
            Shared.hashPath(src / p) match {
              case None => // It is a folder
                Shared.send(agent.stdin.data, Rpc.CreateFolder(p))
              case `remoteHash` => // Local and remote hashes are same. Do nothing
              case Some(_) => // It is a file and content differs
                Shared.send(agent.stdin.data, Rpc.WriteOver(os.read.bytes(src / p), p))
            }
        }
      }

    val agentReader = new Thread(() => {
      while (agent.isAlive()) {
        SyncActor.send(AgentResponse(Shared.receive[Rpc.StatInfo](agent.stdout.data)))
      }
    })

    agentReader.start()

    val watcher = os.watch.watch(
      Seq(src),
      onEvent = _.foreach { p =>
        val path = p.subRelativeTo(src)
        SyncActor.send(ChangedPath(path, !os.exists(src / path)))
      }
    )

    Thread.sleep(Long.MaxValue)
  }

end Sync
