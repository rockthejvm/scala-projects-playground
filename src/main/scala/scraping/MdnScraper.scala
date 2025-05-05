package scraping

import org.jsoup.Jsoup
import scala.jdk.CollectionConverters.*

object MdnScraper {
  def main(args: Array[String]): Unit = {
    val doc = Jsoup.connect("https://developer.mozilla.org/en-US/docs/Web/API").get()
    val links = doc.select("h2#interfaces").nextAll.select("div.index").select("a").asScala

    val linkData = links.map(link => (link.attr("href"), link.attr("title"), link.text))
    println("links:")
    linkData.foreach(println)


    val articles = linkData.take(5).map {
      case (url, tooltip, name) =>
        println("Scraping " + name)
        val doc = Jsoup.connect("https://developer.mozilla.org" + url).get()
        val summary = doc.select("article.main-page-content p").asScala.headOption match {
          case Some(n) => n.text;
          case None => ""
        }
        val methodsAndProperties = doc
          .select("article.main-page-content dl dt")
          .asScala
          .map(el => (el.text, el.nextElementSibling match {
            case null => "";
            case x => x.text
          }))
        (url, tooltip, name, summary, methodsAndProperties)
    }

    articles.foreach(println)
  }
}
