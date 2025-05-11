package sync

import sync.Rpc.StatInfo

sealed trait Msg
case class ChangedPath(value: os.SubPath, deleted: Boolean) extends Msg
case class AgentResponse(value: StatInfo) extends Msg
