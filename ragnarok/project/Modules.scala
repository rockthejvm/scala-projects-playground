import sbt.*
import sbt.Keys.*

object Circe {
  val Version = "0.14.13"

  val Core    = "io.circe" %% "circe-core"    % Version
  val Generic = "io.circe" %% "circe-generic" % Version
  val Parser  = "io.circe" %% "circe-parser"  % Version

  val All: List[ModuleID] = Core :: Generic :: Parser :: Nil
}

object Http4s {
  val Version = "0.23.30"

  val Dsl         = "org.http4s" %% "http4s-dsl"          % Version
  val EmberServer = "org.http4s" %% "http4s-ember-server" % Version
  val EmberClient = "org.http4s" %% "http4s-ember-client" % Version
  val Circe       = "org.http4s" %% "http4s-circe"        % Version

  val All: List[ModuleID] = Dsl :: EmberServer :: EmberClient :: Circe :: Nil
}

object Logging {
  val Logback       = "ch.qos.logback" % "logback-classic" % "1.5.18"
  val Log4catsCore  = "org.typelevel" %% "log4cats-core"   % "2.7.1"
  val Log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j"  % "2.7.1"

  val All: List[ModuleID] = Logback :: Log4catsCore :: Log4catsSlf4j :: Nil
}

object Langchain4j {
  val Version = "1.0.1"

  val Core    = "dev.langchain4j" % "langchain4j"          % Version
  val OpenAi  = "dev.langchain4j" % "langchain4j-open-ai"  % Version
  val EasyRag = "dev.langchain4j" % "langchain4j-easy-rag" % "1.0.1-beta6"

  val All: List[ModuleID] = Core :: OpenAi :: EasyRag :: Nil
}
