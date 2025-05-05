package blog

/**
 * This is to run it locally.
 */
object RunServer extends cask.MainRoutes {
  override val port = 4321

//  Blog_V2.buildBlog()

  val resourcePath = "/Users/daniel/dev/rockthejvm/courses-playground/scala-projects-playground/src/main/resources"
  val blogDir = os.Path(resourcePath) / "blog_out_v2"

  @cask.staticFiles("/")
  def staticFileRoutes() = blogDir.toString

  initialize()
}