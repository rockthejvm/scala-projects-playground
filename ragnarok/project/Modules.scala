import sbt.*
import sbt.Keys.*

object HaoyiLibs {
  val Cask    = "com.lihaoyi" %% "cask"    % "0.10.2"
  val Upickle = "com.lihaoyi" %% "upickle" % "4.2.1"
  val OsLib   = "com.lihaoyi" %% "os-lib"  % "0.11.4"

  val All: List[ModuleID] = Cask :: Upickle :: OsLib :: Nil
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
