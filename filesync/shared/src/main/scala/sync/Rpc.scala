package sync

import os.SubPath
import upickle.default.{ReadWriter, macroRW, readwriter}

sealed trait Rpc

object Rpc {
  implicit val subPathRw: ReadWriter[SubPath] = readwriter[String].bimap[os.SubPath](_.toString, os.SubPath(_))

  case class StatPath(path: os.SubPath) extends Rpc
  implicit val statPathRw: ReadWriter[StatPath] = macroRW

  case class WriteOver(src: Array[Byte], path: os.SubPath) extends Rpc
  implicit val writeOverRw: ReadWriter[WriteOver] = macroRW

  case class CreateFolder(path: os.SubPath) extends Rpc
  implicit val createFolderRw: ReadWriter[CreateFolder] = macroRW

  case class StatInfo(p: os.SubPath, fileHash: Option[Int])
  implicit val statInfoRw: ReadWriter[StatInfo] = macroRW

  case class DeletePath(path: os.SubPath) extends Rpc
  implicit val deletePathRw: ReadWriter[DeletePath] = macroRW

  implicit val msgRw: ReadWriter[Rpc] = macroRW
}
