package scraping

import org.jsoup.Jsoup
import org.jsoup.*

import scala.jdk.CollectionConverters.*

object LobstersScraper {
  case class Comment(author: String, body: String, children: Seq[Comment])

  def main(args: Array[String]): Unit = {
    val doc = Jsoup.parse(os.read(os.resource / "scraping/lobsters.html"))

    case class Comment(author: String, body: String, children: Seq[Comment])

    def recurse(n: nodes.Element): Comment = {
      val s"https://lobste.rs/u/$user" =
        n.select("div.byline > a").asScala.map(_.attr("href")).find(_.contains("/u/")).get
      Comment(
        user,
        n.selectFirst("div.comment_text").text(),
        n.selectFirst("ol.comments").children().asScala.map(recurse).toSeq
      )
    }

    val commentsTree = doc
      .select("ol.comments1 > li.comments_subtree")
      .asScala
      .drop(1) // skip the top-level comment input box
      .map(recurse)
  }
}
