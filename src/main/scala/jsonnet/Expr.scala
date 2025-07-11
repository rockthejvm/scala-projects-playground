package jsonnet

import jsonnet.Parser.expr

sealed trait Expr

object Expr:
  case class Num(value: Int) extends Expr
  case class Str(value: String) extends Expr
  case class Ident(name: String) extends Expr
  case class Plus(left: Expr, right: Expr) extends Expr
  case class Dict(pairs: Map[String, Expr]) extends Expr
  case class Local(name: String, assigned: Expr, body: Expr) extends Expr
  case class Func(argNames: Seq[String], body: Expr) extends Expr
  case class Call(expr: Expr, args: Seq[Expr]) extends Expr

  def evaluate(expr: Expr, scope: Map[String, Value]): Value =
    expr match
      case Expr.Ident(name) => scope(name)
      case Expr.Num(i)      => Value.Num(i)
      case Expr.Str(s)      => Value.Str(s)
      case Expr.Dict(kvs)   => Value.Dict(kvs.map { case (k, v) => (k, evaluate(v, scope)) })

      case Expr.Plus(left, right) =>
        (evaluate(left, scope), evaluate(right, scope)) match
          case (Value.Num(leftNum), Value.Num(rightNum)) => Value.Num(leftNum + rightNum)
          case (Value.Str(leftStr), Value.Str(rightStr)) => Value.Str(leftStr + rightStr)

      case Expr.Local(name, assigned, body) =>
        val assignedValue = evaluate(assigned, scope)
        evaluate(body, scope + (name -> assignedValue))

      case Expr.Call(expr, args) =>
        val Value.Func(call) = evaluate(expr, scope)
        val evaluatedArgs    = args.map(evaluate(_, scope))
        call(evaluatedArgs)

      case Expr.Func(argNames, body) =>
        Value.Func(args => evaluate(body, scope ++ argNames.zip(args).toMap))

  // Use this for printing as a compact json string
  private def serialize(v: Value): String =
    v match
      case Value.Num(i)    => i.toString
      case Value.Str(s)    => s"\"$s\""
      case Value.Dict(kvs) => kvs.map((k, v) => s"\"$k\": ${serialize(v)}").mkString("{", ", ", "}")

  // Use this for pretty printing
  private def serialize2(v: Value): ujson.Value =
    v match
      case Value.Num(i)    => ujson.Num(i)
      case Value.Str(s)    => ujson.Str(s)
      case Value.Dict(kvs) => ujson.Obj.from(kvs.map { case (k, v) => (k, serialize2(v)) })

  def jsonnet(input: String): String =
    // serialize(evaluate(fastparse.parse(input, expr(_)).get.value, Map.empty))
    ujson.write(
      serialize2(evaluate(fastparse.parse(input, Parser.expr(_)).get.value, Map.empty)),
      indent = 2
    )

  def main(args: Array[String]): Unit = {
    Seq(
      evaluate(fastparse.parse("\"hello\"", expr(_)).get.value, Map.empty),
      evaluate(fastparse.parse("""{ "hello": "world", "key": "value" }""", expr(_)).get.value, Map.empty),
      evaluate(fastparse.parse("\"hello\" + \"world\"", expr(_)).get.value, Map.empty),

      // Call
      evaluate(fastparse.parse("""local greeting = "hello "; greeting + greeting""", expr(_)).get.value, Map.empty),
      evaluate(fastparse.parse("""local x = "Hello "; local y = "World"; x + y""", expr(_)).get.value, Map.empty),
      // evaluate(fastparse.parse("""local greeting = "Hello"; nope + nope""", expr(_)).get.value, Map.empty),

      // Func
      evaluate(fastparse.parse("""local f = function(a) a + "1"; f("123")""", expr(_)).get.value, Map.empty),
      evaluate(
        fastparse
          .parse("""local f = function(a, b) a + " " + b; f("hello", "world")""", expr(_))
          .get
          .value,
        Map.empty
      )
    ).foreach(println)

    // jsonnet
    println(jsonnet("""|local greeting = "Hello ";
                       |local person = function (name) {
                       | "name": name,
                       | "welcome": greeting + name + "!"
                       |};
                       |{
                       | "person1": person("Alice"),
                       | "person2": person("Bob"),
                       | "person3": person("Charlie")
                       |}""".stripMargin))

    println(
      jsonnet(
        """|local bonus = 15000;
           |local person = function (name, baseSalary) {
           | "name": name,
           | "totalSalary": baseSalary + bonus
           |};
           |{"person1": person("Alice", 10000), "person2": person("Bob", 20000)}
           |""".stripMargin
      )
    )
  }
end Expr
