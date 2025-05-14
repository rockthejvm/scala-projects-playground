package jsonnet

sealed trait Value

object Value:
  case class Str(value: String) extends Value
  case class Num(value: Int) extends Value
  case class Dict(pairs: Map[String, Value]) extends Value
  case class Func(call: Seq[Value] => Value) extends Value
end Value
