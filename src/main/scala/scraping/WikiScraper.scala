package scraping

import org.jsoup.Jsoup
import scala.jdk.CollectionConverters.*

object WikiScraper {
  def main(args: Array[String]): Unit = {
    val doc = Jsoup.connect("http://en.wikipedia.org/").get()
    val headlines = doc.select("#mp-itn b a").asScala
    headlines.map(_.attr("title")).foreach(println)
  }
}
