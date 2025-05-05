package scraping

import java.util.concurrent.Executors
import scala.annotation.tailrec
import scala.concurrent.*
import scala.concurrent.duration.*

object ParallelCrawler {
  // https://www.mediawiki.org/wiki/API:Tutorial
  def fetchLinks(title: String): Seq[String] = {
    val resp = requests.get(
      "https://en.wikipedia.org/w/api.php",
      params = Seq(
        "action" -> "query",
        "titles" -> title,
        "prop" -> "links",
        "format" -> "json"
      ))
    for {
      page <- ujson.read(resp)("query")("pages").obj.values.toSeq
      links <- page.obj.get("links").toSeq
      link <- links.arr
    } yield link("title").str
  }

  // fetch all links sequentially (iterative or recursive)
  def fetchAllLinks(startTitle: String, depth: Int): Set[String] = {
    var seen = Set(startTitle)
    var current = Set(startTitle)
    for (i <- Range(0, depth)) {
      val nextTitleLists = current.map(fetchLinks)
      current = nextTitleLists.flatten.filter(!seen.contains(_))
      seen = seen ++ current
    }
    seen
  }

  // continuing with the iterative solution
  def fetchAllLinksPar(startTitle: String, depth: Int)(using ExecutionContext): Set[String] = {
    var seen = Set(startTitle)
    var current = Set(startTitle)
    for (i <- Range(0, depth)) {
      val futures = current.map(t => Future(fetchLinks(t)))
      val nextTitleLists = futures.map(Await.result(_, Duration.Inf))
      current = nextTitleLists.flatten.filter(!seen.contains(_))
      seen = seen ++ current
    }
    seen
  }

  def fetchAllLinksRecPar(startTitle: String, depth: Int)(using ExecutionContext) = {
    @tailrec
    def aux(level: Int = 0, seen: Set[String] = Set(), current: Set[String] = Set()): Set[String] =
      if (level >= depth) seen
      else {
        val futures = current.map(t => Future(fetchLinks(t)))
        val nextTitleLists = futures.flatMap(Await.result(_, Duration.Inf).filter(!seen.contains(_)))
        aux(level + 1, seen ++ nextTitleLists, nextTitleLists)
      }

    aux(0, Set(startTitle), Set(startTitle))
  }

  // if you want to make it asynchronous
  val asyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient()

  def fetchLinksAsync(title: String)(using ExecutionContext): Future[Seq[String]] = {
    val p = Promise[String]
    val listenableFut = asyncHttpClient.prepareGet("https://en.wikipedia.org/w/api.php")
      .addQueryParam("action", "query").addQueryParam("titles", title)
      .addQueryParam("prop", "links").addQueryParam("format", "json")
      .execute()
    listenableFut.addListener(() => p.success(listenableFut.get().getResponseBody), null)
    val scalaFut: Future[String] = p.future
    scalaFut.map { responseBody =>
      for {
        page <- ujson.read(responseBody)("query")("pages").obj.values.toSeq
        links <- page.obj.get("links").toSeq
        link <- links.arr
      } yield link("title").str
    }
  }


  def fetchAllLinksAsync(startTitle: String, depth: Int)(using ExecutionContext): Future[Set[String]] = {
    def rec(current: Set[String], seen: Set[String], recDepth: Int): Future[Set[String]] = {
      if (recDepth >= depth) Future.successful(seen)
      else {
        val futures = current.map(title => fetchLinksAsync(title))
        Future.sequence(futures).map { nextTitleLists =>
          val nextTitles = nextTitleLists.flatten
          rec(nextTitles.filter(!seen.contains(_)), seen ++ nextTitles, recDepth + 1)
        }.flatten
      }
    }

    rec(Set(startTitle), Set(startTitle), 0)
  }

  def main(args: Array[String]): Unit = {
    given ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
    fetchAllLinksPar("Singapore", 2).foreach(println)
  }
}
