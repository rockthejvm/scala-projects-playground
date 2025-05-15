package chat.backend

object Labs extends App {
  val data = "name=&msg="

  private val interim = data
    .split("&")
    .map(_.split("=", 2))

  val params =
    interim.map { case Array(k, v) => (k, v) }.toMap

  println(params)
}
