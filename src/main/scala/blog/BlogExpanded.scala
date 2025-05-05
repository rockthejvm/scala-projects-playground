package blog

import os.{Path, ReadablePath}
import scalatags.Text.all.*

/*
  This is generated fully with AI, from the V1.
  The prompt was:
    This file (Blog V1) is a simple static blog generator with basic styles.
    Help me expand the functionality to generate a complete modern programmer's blog,
      with static pages like "about" and "contact" (and maybe others),
      and also all the blog articles styled nicely and separately.
    Figure out the necessary pages and structure to have a clean and modern feel,
      and include necessary styles for the different markdown sections we have in `resources/blog`.
 */
object BlogExpanded {

  // Config section
  val siteName        = "Daniel's Programming Blog"
  val siteDescription = "Thoughts on Scala, Java, and functional programming"
  val githubUrl       = "https://github.com/daniel-ciocirlan"
  val twitterUrl      = "#"
  val linkedinUrl     = "#"

  // File helpers
  def mdNameToHtml(name: String): String = name.replace(" ", "-").toLowerCase + ".html"

  // UI Components
  def navbar(currentPage: String = ""): Tag = {
    div(cls := "navbar navbar-expand-lg navbar-dark bg-primary")(
      div(cls := "container")(
        a(cls := "navbar-brand", href := "/index.html")(siteName),
        button(
          cls                 := "navbar-toggler",
          attr("type")        := "button",
          attr("data-toggle") := "collapse",
          attr("data-target") := "#navbarNav"
        )(
          span(cls := "navbar-toggler-icon")
        ),
        div(cls := "collapse navbar-collapse", id := "navbarNav")(
          ul(cls := "navbar-nav ml-auto")(
            li(cls := s"nav-item ${if (currentPage == "home") "active" else ""}")(
              a(cls := "nav-link", href := "/index.html")("Home")
            ),
            li(cls := s"nav-item ${if (currentPage == "articles") "active" else ""}")(
              a(cls := "nav-link", href := "/articles.html")("Articles")
            ),
            li(cls := s"nav-item ${if (currentPage == "about") "active" else ""}")(
              a(cls := "nav-link", href := "/about.html")("About")
            ),
            li(cls := s"nav-item ${if (currentPage == "contact") "active" else ""}")(
              a(cls := "nav-link", href := "/contact.html")("Contact")
            )
          )
        )
      )
    )
  }

  def makeFooter: Tag = {
    footer(cls := "footer mt-auto py-3 bg-light")(
      div(cls := "container text-center")(
        div(cls := "social-links mb-3")(
          a(cls   := "mx-2", href := githubUrl, target := "_blank")(
            i(cls := "fab fa-github fa-2x")
          ),
          a(cls   := "mx-2", href := twitterUrl, target := "_blank")(
            i(cls := "fab fa-twitter fa-2x")
          ),
          a(cls   := "mx-2", href := linkedinUrl, target := "_blank")(
            i(cls := "fab fa-linkedin fa-2x")
          )
        ),
        p(cls := "text-muted")(
          s"© ${java.time.Year.now().getValue} $siteName. Built with Scala."
        )
      )
    )
  }

  def layout(t: String, c: Seq[Tag], currentPage: String = ""): Tag = {
    html(lang := "en", cls := "h-100")(
      head(
        title := s"$t | $siteName",
        meta(charset := "UTF-8"),
        meta(name    := "viewport", content    := "width=device-width, initial-scale=1.0"),
        meta(name    := "description", content := siteDescription),
        link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css"),
        link(rel := "stylesheet", href := "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css"),
        link(
          rel  := "stylesheet",
          href := "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/atom-one-dark.min.css"
        ),
        link(rel := "stylesheet", href := "/css/style.css")
      ),
      body(cls := "d-flex flex-column h-100")(
        navbar(currentPage),
        div(cls := "flex-shrink-0")(
          div(cls := "container my-4")(
            c
          )
        ),
        makeFooter,
        script(src := "https://code.jquery.com/jquery-3.5.1.slim.min.js"),
        script(src := "https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"),
        script(src := "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js"),
        script("hljs.highlightAll();")
      )
    )
  }

  // Page generation
  def generateHomePage(outPath: Path, featuredPosts: Seq[(String, String, Path, java.time.LocalDate)]): Unit = {
    val content = Seq(
      div(cls := "jumbotron bg-primary text-white")(
        h1(cls := "display-4")("Welcome to my Programming Blog"),
        p(cls := "lead")(siteDescription),
        hr(cls := "my-4"),
        p("Explore my articles on Scala, functional programming, and more"),
        a(cls := "btn btn-light btn-lg", href := "/articles.html")("Browse Articles")
      ),
      div(cls := "row")(
        div(cls := "col-lg-8")(
          h2(cls := "mb-4")("Latest Articles"),
          featuredPosts.take(3).map { case (_, suffix, _, timestamp) =>
            div(cls := "card mb-4")(
              div(cls := "card-body")(
                h3(cls := "card-title")(a(href := s"/post/${mdNameToHtml(suffix)}")(suffix)),
                p(cls   := "card-text text-muted")(
                  i(cls := "far fa-calendar-alt mr-2"),
                  timestamp.toString
                ),
                a(href := s"/post/${mdNameToHtml(suffix)}", cls := "btn btn-primary")("Read More")
              )
            )
          }
        ),
        div(cls := "col-lg-4")(
          div(cls := "card")(
            div(cls := "card-body")(
              h4(cls := "card-title")("About Me"),
              p(cls := "card-text")(
                "I'm a passionate programmer focusing on functional programming and Scala ecosystem."
              ),
              a(href := "/about.html", cls := "btn btn-outline-primary")("Learn More")
            )
          )
        )
      )
    )

    os.write(
      outPath / "index.html",
      layout("Home", content, "home").render
    )
  }

  def generateArticlesPage(outPath: Path, posts: Seq[(String, String, Path, java.time.LocalDate)]): Unit = {
    val content = Seq(
      h1(cls := "mb-4")("All Articles"),
      div(cls := "row")(
        posts.map { case (_, suffix, _, timestamp) =>
          div(cls := "col-md-6 mb-4")(
            div(cls := "card h-100")(
              div(cls := "card-body")(
                h3(cls := "card-title")(a(href := s"/post/${mdNameToHtml(suffix)}")(suffix)),
                p(cls   := "card-text text-muted")(
                  i(cls := "far fa-calendar-alt mr-2"),
                  timestamp.toString
                )
              ),
              div(cls := "card-footer bg-white border-0")(
                a(href := s"/post/${mdNameToHtml(suffix)}", cls := "btn btn-primary")("Read More")
              )
            )
          )
        }
      )
    )

    os.write(
      outPath / "articles.html",
      layout("Articles", content, "articles").render
    )
  }

  def generateAboutPage(outPath: Path): Unit = {
    val content = Seq(
      h1(cls := "mb-4")("About Me"),
      div(cls := "row")(
        div(cls   := "col-md-4")(
          img(cls := "img-fluid rounded mb-4", src := "/img/profile.jpg", alt := "Profile Picture")
        ),
        div(cls := "col-md-8")(
          p(
            "I'm a software developer passionate about functional programming, Scala, and building robust applications."
          ),
          p("With experience in backend development, I enjoy writing clean code and solving complex problems."),
          h3("Skills"),
          ul(
            li("Scala & Functional Programming"),
            li("Java Ecosystem"),
            li("Backend Development"),
            li("Distributed Systems"),
            li("Web Development")
          ),
          h3("Background"),
          p(
            "I have been working with Scala for several years, focusing on building scalable and maintainable applications. My background includes experience with Akka, Play Framework, and other JVM technologies."
          )
        )
      )
    )

    os.write(
      outPath / "about.html",
      layout("About", content, "about").render
    )
  }

  def generateContactPage(outPath: Path): Unit = {
    val content = Seq(
      h1(cls := "mb-4")("Contact Me"),
      div(cls := "row")(
        div(cls := "col-md-6")(
          p("Feel free to reach out to me through any of the following channels:"),
          ul(cls := "list-unstyled")(
            li(cls  := "mb-3")(
              i(cls := "fas fa-envelope mr-2"),
              a(href := "mailto:contact@example.com")("contact@example.com")
            ),
            li(cls  := "mb-3")(
              i(cls := "fab fa-github mr-2"),
              a(href := githubUrl, target := "_blank")("GitHub")
            ),
            li(cls  := "mb-3")(
              i(cls := "fab fa-linkedin mr-2"),
              a(href := linkedinUrl, target := "_blank")("LinkedIn")
            ),
            li(cls  := "mb-3")(
              i(cls := "fab fa-twitter mr-2"),
              a(href := twitterUrl, target := "_blank")("Twitter")
            )
          )
        ),
        div(cls := "col-md-6")(
          div(cls := "card")(
            div(cls := "card-body")(
              h3(cls := "card-title")("Send a Message"),
              form(
                div(cls := "form-group")(
                  label(`for` := "name")("Name"),
                  input(cls := "form-control", id := "name", attr("type") := "text", placeholder := "Your name")
                ),
                div(cls := "form-group")(
                  label(`for` := "email")("Email"),
                  input(cls := "form-control", id := "email", attr("type") := "email", placeholder := "Your email")
                ),
                div(cls := "form-group")(
                  label(`for` := "message")("Message"),
                  textarea(cls := "form-control", id := "message", rows := "5", placeholder := "Your message")
                ),
                button(cls := "btn btn-primary", attr("type") := "submit")("Send Message")
              )
            )
          )
        )
      )
    )

    os.write(
      outPath / "contact.html",
      layout("Contact", content, "contact").render
    )
  }

  def generateCustomCss(outPath: Path): Unit = {
    val css = """
                |/* Custom styles */
                |body {
                |  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
                |  color: #333;
                |  line-height: 1.6;
                |}
                |
                |/* Post styling */
                |.post-content {
                |  max-width: 800px;
                |  margin: 0 auto;
                |}
                |
                |.post-content h1, .post-content h2, .post-content h3, .post-content h4 {
                |  margin-top: 2rem;
                |  margin-bottom: 1rem;
                |  font-weight: 600;
                |}
                |
                |.post-content p {
                |  margin-bottom: 1.5rem;
                |}
                |
                |.post-content img {
                |  max-width: 100%;
                |  height: auto;
                |  display: block;
                |  margin: 2rem auto;
                |}
                |
                |.post-content pre {
                |  border-radius: 5px;
                |  margin: 1.5rem 0;
                |}
                |
                |.post-content code {
                |  padding: 0.2em 0.4em;
                |  background-color: rgba(0, 0, 0, 0.05);
                |  border-radius: 3px;
                |}
                |
                |.post-content blockquote {
                |  border-left: 4px solid #007bff;
                |  padding-left: 1rem;
                |  color: #6c757d;
                |  font-style: italic;
                |}
                |
                |/* Custom components */
                |.jumbotron {
                |  border-radius: 0;
                |  margin-bottom: 2rem;
                |}
                |
                |.card {
                |  border: none;
                |  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                |  transition: transform 0.2s ease;
                |}
                |
                |.card:hover {
                |  transform: translateY(-5px);
                |}
                |
                |.footer {
                |  padding: 2rem 0;
                |  margin-top: 3rem;
                |}
                |
                |.social-links a {
                |  color: #495057;
                |  transition: color 0.2s ease;
                |}
                |
                |.social-links a:hover {
                |  color: #007bff;
                |}
                |""".stripMargin

    os.makeDir.all(outPath / "css")
    os.write(outPath / "css" / "style.css", css)
  }

  def generatePostPage(outPath: Path, suffix: String, htmlContent: String, timestamp: java.time.LocalDate): Unit = {
    val content = Seq(
      div(cls := "post-header mb-4")(
        h1(cls := "display-4")(suffix),
        p(cls   := "text-muted")(
          i(cls := "far fa-calendar-alt mr-2"),
          timestamp.toString
        )
      ),
      div(cls := "post-content")(
        raw(htmlContent)
      ),
      hr(),
      div(cls := "post-footer")(
        div(cls := "d-flex justify-content-between align-items-center")(
          a(href := "/articles.html", cls := "btn btn-outline-primary")("← All Articles"),
          div(cls := "social-share")(
            span(cls := "mr-2")("Share:"),
            a(cls   := "btn btn-sm btn-outline-secondary mr-1", href := "#")(
              i(cls := "fab fa-twitter")
            ),
            a(cls   := "btn btn-sm btn-outline-secondary mr-1", href := "#")(
              i(cls := "fab fa-facebook")
            ),
            a(cls   := "btn btn-sm btn-outline-secondary", href := "#")(
              i(cls := "fab fa-linkedin")
            )
          )
        )
      )
    )

    os.write(
      outPath / "post" / mdNameToHtml(suffix),
      layout(suffix, content, "articles").render
    )
  }

  def buildBlog(): Unit = {
    val resourcePath = (os.pwd / "src/main/resources").toString
    val blogRoot: Path = os.Path(resourcePath) / "blog"
    val outPath:  Path = os.Path(resourcePath) / "blog_out_v2"

    val postInfo = os.list
      .apply(blogRoot)
      .map { p =>
        val s"$prefix - $suffix.md" = p.last
        val publishDate = java.time.LocalDate.ofInstant(
          java.time.Instant.ofEpochMilli(os.mtime(p)),
          java.time.ZoneOffset.UTC
        )
        (prefix, suffix, p, publishDate)
      }
      .sortBy(_._1.toInt)
      .reverse // Most recent first

    // Setup output directory structure
    os.remove.all(outPath)
    os.makeDir.all(outPath / "post")
    os.makeDir.all(outPath / "img")

    // Copy a placeholder profile image
    // In a real scenario, you'd have this file in resources
    os.write(
      outPath / "img" / "profile.jpg",
      "Placeholder for profile image - in a real project, copy an actual image file here"
    )

    // Generate custom CSS
    generateCustomCss(outPath)

    // Generate blog post pages
    postInfo.foreach { case (_, suffix, path, timestamp) =>
      val parser   = org.commonmark.parser.Parser.builder().build()
      val document = parser.parse(os.read(path))
      val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
      val output   = renderer.render(document)

      generatePostPage(outPath, suffix, output, timestamp)
    }

    // Generate static pages
    generateHomePage(outPath, postInfo)
    generateArticlesPage(outPath, postInfo)
    generateAboutPage(outPath)
    generateContactPage(outPath)
  }

  def main(args: Array[String]): Unit = {
    args.headOption match {
      case Some("buildBlog") => buildBlog()
      case _                 => println("Unknown command")
    }
  }
}
