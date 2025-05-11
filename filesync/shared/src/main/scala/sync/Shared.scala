package sync

import upickle.default.{Reader, Writer}

import java.io.{DataInputStream, DataOutputStream}

object Shared:
  def send[T: Writer](out: DataOutputStream, msg: T): Unit =
    val bytes = upickle.default.writeBinary(msg)
    out.writeInt(bytes.length)
    out.write(bytes)
    out.flush()

  def receive[T: Reader](in: DataInputStream): T =
    val buf = new Array[Byte](in.readInt())
    in.readFully(buf)
    upickle.default.readBinary[T](buf)

  def hashPath(p: os.Path): Option[Int] =
    if (!os.isFile(p)) None
    else Some(java.util.Arrays.hashCode(os.read.bytes(p)))
end Shared
