package scraping

import org.jsoup.Jsoup

import scala.collection.parallel.CollectionConverters.*
import scala.jdk.CollectionConverters.*

case class Article(title: String, url: String, tags: Seq[String])

/**
 * Crawls the RockTheJVM blog posts by chunking and scraping in parallel, and returns a map of tags to articles
 */
object RockTheJVM extends App {
  private val noOfPages = scrapNoOfPages()
  println(s"NoOfPages: $noOfPages")

  private val tagArticlesMap: Map[String, List[Article]] =
    (1 to noOfPages)
      .grouped(5)
      .toVector
      .par
      .flatMap { group =>
        println(s"Processing batch: ${group.min} to ${group.max}")

        group.flatMap { page =>
          println(s"Processing page: https://rockthejvm.com/articles/$page")
          Jsoup
            .connect(s"https://rockthejvm.com/articles/$page")
            .get()
            .select("article")
            .asScala
            .map { article =>
              val title = article.select("h2").text()
              val url   = article.select("a[href^=\"/articles/\"]").attr("href")
              val tags  = article.select("div>a[href^=\"/tags/\"]").asScala.map(_.text()).toList
              Article(title, url, tags)
            }
        }
      }
      .flatMap(article => article.tags.map(tag => (tag = tag, article = article))) // Using named tuples :)
      .seq // Convert back to a sequential collection
      .groupMap(_.tag)(_.article)
      .view
      .mapValues(_.toList)
      .toMap

  private def scrapNoOfPages(): Int =
    Jsoup
      .connect("https://rockthejvm.com/articles/1")
      .get()
      .select("footer>nav>div.hidden")
      .first()
      .select("a[href*=\"/articles/\"]:last-child")
      .text()
      .toInt

  println(tagArticlesMap.mkString("\n"))
}
