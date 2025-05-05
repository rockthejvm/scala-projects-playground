package imageKernelFilters.v1

case class Window(width: Int, height: Int, values: Array[Pixel]) {

  // just a sanity check method, will be handy to use in part 3 later
  def hasSameSizeAs(kernel: Kernel): Boolean =
    width == kernel.width && height == kernel.height && values.length == kernel.values.length

  def multiply(kernel: Kernel): Pixel = {
    /*
      YOUR CODE HERE (4 - 20 lines, depending on approach)

      - separate the red, green and blue channel values from all the pixels into an array for each
      - do an element-wise product between each channel's array and the kernel values
      - sum up the products
      - create a Pixel with the resulting red, green and blue values (which should be each between 0 and 1)
    */
    if (!hasSameSizeAs(kernel)) throw new IllegalArgumentException("Kernel and window have different sizes")
    else {
      val red = values.map(_.r).zip(kernel.values).map(tuple => tuple._1 * tuple._2).sum
      val green = values.map(_.g).zip(kernel.values).map(tuple => tuple._1 * tuple._2).sum
      val blue = values.map(_.b).zip(kernel.values).map(tuple => tuple._1 * tuple._2).sum
      Pixel.clamp(red, green, blue)
    }
  }

  // a handy toString method
  override def toString: String = {
    val lines = (0 until height).map(y => values.slice(width * y, width * (y + 1)).mkString(" "))
    s"""size ${values.length}:
        |${lines.mkString("\n")}"
        |""".stripMargin
  }
}

// maybe turn this into an actual test
object WindowTest {
    import scala.util.Random

    // these pixels are (0,0,blue), where blue goes from 0.1 (dark) to 0.9 (bright)
    val simplePixels = (1 to 9).map(x => Pixel(0, 0, 0.1 * x)).toArray
    val simpleWindow = new Window(3, 3, simplePixels)
    val simpleBlurredPixel = simpleWindow.multiply(Kernel.blur3)


    val r = new Random(200)
    val pixels = (1 to 9).map(_ => Pixel(r.nextDouble(),r.nextDouble(),r.nextDouble())).toArray
    val window = new Window(3, 3, pixels)

    val blurredPixel = window.multiply(Kernel.blur3)


    def main(args: Array[String]): Unit = {
        /*
            Expected result:

            (0,0,128)
            subjectively a pretty dark blue (halfway through black and brightest blue)
        */
        println(s"${simpleBlurredPixel.red}, ${simpleBlurredPixel.green}, ${simpleBlurredPixel.blue}")

        /*
            Expected result:

            (180,151,157)
            a soft pink-brown color
        */
        println(s"${blurredPixel.red}, ${blurredPixel.green}, ${blurredPixel.blue}")
    }
}