package imageKernelFilters.v2

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

case class Pixel(r: Int, g: Int, b: Int) {
  val red: Int = Pixel.clampColor(r)
  val green: Int = Pixel.clampColor(g)
  val blue: Int = Pixel.clampColor(b)

  def toInt: Int =
    (red << 16) | (green << 8) | blue

  def draw(width: Int, height: Int, path: String): Unit = {
    val pixelInt = toInt
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val pixels = Array.fill(width * height)(pixelInt)
    image.setRGB(0, 0, width, height, pixels, 0, width)
    ImageIO.write(image, "JPG", new File(path))
  }

  def +(other: Pixel): Pixel =
    Pixel(
      Pixel.clampColor(red + other.red),
      Pixel.clampColor(green + other.green),
      Pixel.clampColor(blue + other.blue)
    )
}

object Pixel {
  def clampColor(v: Int): Int =
    if (v <= 0) 0
    else if (v >= 255) 255
    else v

  val BLACK: Pixel = Pixel(0, 0, 0)
  val RED: Pixel = Pixel(255, 0, 0)
  val GREEN: Pixel = Pixel(0, 255, 0)
  val BLUE: Pixel = Pixel(0, 0, 255)
  val GRAY: Pixel = Pixel(128, 128, 128)

  def fromHex(arg: Int): Pixel = {
    val red = (arg & 0xFF0000) >> 16
    val green = (arg & 0xFF00) >> 8
    val blue = arg & 0xFF
    Pixel(red, green, blue)
  }
}