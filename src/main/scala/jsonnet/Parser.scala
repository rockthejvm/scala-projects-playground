package jsonnet

import fastparse.*
import MultiLineWhitespace.*

object Parser:

  def str0[p: P]:   P[String]     = P("\"" ~~/ CharsWhile(_ != '"', 0).! ~~ "\"")
  def str[p: P]:    P[Expr.Str]   = P(str0).map(Expr.Str.apply)
  def ident0[p: P]: P[String]     = P(CharIn("a-zA-Z_") ~~ CharsWhileIn("a-zA-z0-9_", 0)).!
  def ident[p: P]:  P[Expr.Ident] = P(ident0).map(Expr.Ident.apply)
  def local[p: P]:  P[Expr.Local] = P("local" ~/ ident0 ~ "=" ~ expr ~ ";" ~ expr).map(Expr.Local.apply)
  def func[p: P]:   P[Expr.Func]  = P("function" ~/ "(" ~ ident0.rep(0, ",") ~ ")" ~ expr).map(Expr.Func.apply)

  def dict[p: P]: P[Expr.Dict] = P("{" ~/ (str0 ~ ":" ~/ expr).rep(0, ",") ~/ "}").map(kvs => Expr.Dict(kvs.toMap))

  def callExpr[p: P]: P[Expr]      = P(str | dict | local | func | ident)
  def call[p: P]:     P[Seq[Expr]] = P("(" ~/ expr.rep(0, ",") ~ ")")

  def prefixExpr[p: P]: P[Expr] = P(callExpr ~ call.rep).map { case (left, items) =>
    items.foldLeft(left)(Expr.Call.apply)
  }

  def plus[p: P]: P[Expr] = P("+" ~ prefixExpr)
  def expr[p: P]: P[Expr] = P(prefixExpr ~ plus.rep).map { case (left, rights) =>
    rights.foldLeft(left)(Expr.Plus.apply)
  }

  def main(args: Array[String]): Unit = {
    Seq( // str
      fastparse.parse("\"hello\"", str(_)),
      fastparse.parse("\"hello world\"", str(_)),
      fastparse.parse("\"\"", str(_)),
      fastparse.parse("123", str(_))
    ).foreach(println)

    Seq( // ident
      fastparse.parse("hello", ident(_)),
      fastparse.parse("_world", ident(_)),
      fastparse.parse("hello world", ident(_)),
      fastparse.parse("123", ident(_))
    ).foreach(println)

    Seq( // plus
      fastparse.parse("\"hello\" + \"world\"", expr(_)),
      fastparse.parse("\"hello\" + \" \" + \"world\"", expr(_)),
      fastparse.parse("a + b", expr(_)),
      fastparse.parse("""a + " " + c""", expr(_))
    ).foreach(println)

    Seq( // dict
      fastparse.parse("""{"a": "b", "cde": id, "nested": {}}""", dict(_)),
      fastparse.parse("""{"a": "A", "b": "bee"}""", expr(_)),
      fastparse.parse("""f()(a) + g(b, c)""", expr(_)),
      fastparse.parse("""local thing = "kay"; { "f": function(a) a + a, "nested": {"k": "v"}}""", expr(_))
    ).foreach(println)
  }
