package blog

import os.{Path, ReadablePath}
import scalatags.Text.all.*

object Blog {

  def mdNameToHtml(name: String) = name.replace(" ", "-").toLowerCase + ".html"

  def buildBlog(): Unit = {
    val resourcePath = "/Users/daniel/dev/rockthejvm/courses-playground/scala-projects-playground/src/main/resources"
    val blogRoot: Path = os.Path(resourcePath) / "blog"
    val outPath: Path = os.Path(resourcePath) / "blog_out"
    
    val postInfo = os.list.apply(blogRoot)
      .map { p =>
        val s"$prefix - $suffix.md" = p.last
        val publishDate = java.time.LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(os.mtime(p)),
            java.time.ZoneOffset.UTC
          )
        (prefix, suffix, p, publishDate)
      }
      .sortBy(_._1.toInt)

    val bootstrapCss = link(
      rel := "stylesheet",
      href := "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
    )

    os.remove.all(outPath)
    os.makeDir.all(outPath / "post")

    postInfo.foreach {
      case (_, suffix, path, timestamp) =>
        val parser = org.commonmark.parser.Parser.builder().build()
        val document = parser.parse(os.read(path))
        val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
        val output = renderer.render(document)
        os.write(
          outPath / "post" / mdNameToHtml(suffix),
          doctype("html")(
            html(
              head(bootstrapCss),
              body(
                h1(a(href := "../index.html")("Blog"), " / ", suffix),
                raw(output),
                p(i("Written on " + timestamp))
              )
            )
          )
        )
    }

    os.write(
      outPath / "index.html",
      doctype("html")(
        html(
          head(bootstrapCss),
          body(
            h1("Blog"),
            postInfo.map {
              case (_, suffix, _, _) =>
                h2(a(href := ("post/" + mdNameToHtml(suffix)))(suffix))
            }
          )
        )
      )
    )
  }

  def main(args: Array[String]): Unit = {
    buildBlog()
  }
}

/**
 * This is to run it locally.
 */
object Run extends cask.MainRoutes {
  override val port = 4321

  BlogExpanded.buildBlog()

  val resourcePath = "/Users/daniel/dev/rockthejvm/courses-playground/scala-projects-playground/src/main/resources"
  val blogDir = os.Path(resourcePath) / "blog_out_v2"

  @cask.staticFiles("/")
  def staticFileRoutes() = blogDir.toString

  initialize()
}
