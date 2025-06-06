package scrape

import org.jsoup.Jsoup

import scala.collection.parallel.CollectionConverters.*
import scala.jdk.CollectionConverters.*

case class Headline(title: String, url: String)

object Guardian:
  private final val UserAgent =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.101.76 Safari/537.36"

  private val pageSelectorMap =
    "https://www.theguardian.com/us"         -> "#container-news>ul>li a" ::
      "https://www.theguardian.com/world"    -> "div[id *= container-]>ul>li a" ::
      "https://www.theguardian.com/us/sport" -> "div#container-sports>ul>li a" ::
      Nil

  def scrapeHeadlines(): Seq[Headline] =
    pageSelectorMap.par.flatMap { case (url, selector) =>
      Jsoup
        .connect(url)
        .userAgent(UserAgent)
        .get()
        .select(selector)
        .asScala
        .map { a =>
          val title = if a.text().isEmpty then a.attr("aria-label") else a.text
          Headline(title, a.attr("href"))
        }
    }.seq
