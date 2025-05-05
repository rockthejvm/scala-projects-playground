package imageKernelFilters.v1

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Image private (val imgData: BufferedImage) {
  private def this(path: String) =
    this(ImageIO.read(new File(path)))

  private def this(width: Int, height: Int, colors: Array[Int]) = this {
    import Image.*
    val bfImg = new BufferedImage(width, height, IMAGE_TYPE)
    bfImg.setRGB(0, 0, width, height, colors, 0, width)
    bfImg
  }

  // now we're free to inspect the properties of the image
  val width = imgData.getWidth
  val height = imgData.getHeight

  // this value is important as it specifies the color type
  // we'll need to pass it to the resulting images as well
  val imageType = imgData.getType

  // jpegs will not have alpha (transparency)
  // however, if you want to modify this code to support PNGs for example, they will have alpha as well
  val hasAlpha = imgData.getColorModel.hasAlpha

  // the actual binary data
  // each pixel is an int: alpha|red|green|blue, each channel being one byte
  val pixels = imgData.getRGB(0, 0, width, height, new Array[Int](width * height), 0, width)

  def write(path: String): Unit =
    ImageIO.write(imgData, "JPG", new File(path))

  /**
   * Later
   */

  /*
    An overloaded window method which conveniently calculates the offsetX and offsetY for you given a kernel.
    Feel free to use it if you wish.
*/
  def window(x: Int, y: Int, k: Kernel): Window =
    window(x, y, k.width / 2, k.height / 2)

  /*
      This method extracts a window centered at (x, y) with offsetX and offsetY on each axis.
  */
  def window(x: Int, y: Int, offsetX: Int, offsetY: Int): Window = {
    /*
        YOUR CODE HERE (5 - 10 lines, depending on your approach)

        1. for each x position between x - offsetX and x + offsetX, and similar for y,
        obtain a pixel value as pixels(width * xPixel + yPixel)
            ** take care not to cross image boundaries
            ** make sure to take the pixels in the proper order: go through rows(y) first, then columns(x)

        2. create a new Window object out of the pixel values
    */
    val pixelIndices = for {
      yh <- (y - offsetY) to (y + offsetY) // yh for "hypothetical"
      xh <- (x - offsetX) to (x + offsetX)
      xp = if (xh <= 0) 0 else if (xh >= width) width - 1 else xh // xp = coordinate of the actual pixel
      yp = if (yh <= 0) 0 else if (yh >= height) height - 1 else yh
    } yield yp * width + xp // index in the pixel array

    Window(2 * offsetX + 1, 2 * offsetY + 1, pixelIndices.toArray.map(index => new Pixel(pixels(index))))
  }

  def apply(kernel: Kernel): Image = {
    import Image.*
    /*
        YOUR CODE HERE (3 - 5 lines)

        - for each x, y position (x goes 0 until width, y goes 0 until height)
        create a window, then multiply with the kernel

        - create a new ImageData object out of the resulting pixel array
    */
    val newPixels = for {
      y <- (0 until height).toArray
      x <- (0 until width).toArray
      w = window(x, y, kernel)
      pixel = w.multiply(kernel).toInt
    } yield pixel

    val newBitmap = new BufferedImage(width, height, IMAGE_TYPE)
    newBitmap.setRGB(0,0,width, height, newPixels,0, width)
    new Image(newBitmap)
  }

  def apply(kernels: List[Kernel]): Image =
    kernels.foldLeft(this)((img, kernel) => img.apply(kernel))

  // TODO perhaps refactor this into some modifier classes (including the kernel filters)
  def mirrorVertical(): Image = {
    val newColors = for {
      y <- 0 until height
      x <- 0 until width
    } yield pixels(width * (height - y - 1) + x)

    new Image(width, height, newColors.toArray)
  }

  def mirrorHorizontal(): Image = {
    val newColors = for {
      y <- 0 until height
      x <- 0 until width
    } yield pixels(width * y + (width - x - 1))

    new Image(width, height, newColors.toArray)
  }

  def rotateRight(): Image = {
    val newColors = for {
      y <- 0 until width
      x <- 0 until height
      ox = y
      oy = height - x - 1
    } yield pixels(width * oy + ox)
    new Image(height, width, newColors.toArray)
  }

  def rotateLeft(): Image = {
    val newColors = for {
      y <- 0 until width
      x <- 0 until height
      ox = width - y - 1
      oy = x
    } yield pixels(width * oy + ox)
    new Image(height, width, newColors.toArray)
  }

  // limits exclusive
  def crop(topLeftX: Int, topLeftY: Int, bottomRightX: Int, bottomRightY: Int): Image = {
    assert(topLeftX >= 0 && topLeftX <= width, "top left x coordinate outside image boundaries")
    assert(topLeftY >= 0 && topLeftY <= height, "top left y coordinate outside image boundaries")
    assert(bottomRightX >= 0 && bottomRightX <= width, "bottom right x coordinate outside image boundaries")
    assert(bottomRightY >= 0 && bottomRightY <= height, "bottom right y coordinate outside image boundaries")
    assert(topLeftX < bottomRightX, "top left x should be smaller than bottom right x")
    assert(topLeftY < bottomRightY, "top left y should be smaller than bottom right y")

    val newColors = for {
      y <- topLeftY until bottomRightY
      x <- topLeftX until bottomRightX
    } yield pixels(width * y + x)

    new Image(bottomRightX - topLeftX, bottomRightY - topLeftY, newColors.toArray)
  }

  // the other image decides the canvas
  def onTopOf(another: Image, topLeftX: Int, topLeftY: Int): Image = {
    assert(topLeftX >= 0 && topLeftX <= width, "top left x coordinate outside image boundaries")
    assert(topLeftY >= 0 && topLeftY <= height, "top left y coordinate outside image boundaries")

    val newColors = for {
      y <- 0 until another.height
      x <- 0 until another.width
      xInner = x - topLeftX
      yInner = y - topLeftY
      isInner = xInner >= 0 && xInner < width && yInner >= 0 && yInner < height
    } yield
      if (isInner) pixels(width * yInner + xInner)
      else another.pixels(another.width * y + x)

    new Image(another.width, another.height, newColors.toArray)
  }

  def blend(another: Image, topLeftX: Int, topLeftY: Int, blendMode: BlendMode): Image = {
    assert(topLeftX >= 0 && topLeftX <= width, "top left x coordinate outside image boundaries")
    assert(topLeftY >= 0 && topLeftY <= height, "top left y coordinate outside image boundaries")

    val newColors = for {
      y <- 0 until another.height
      x <- 0 until another.width
      xInner = x - topLeftX
      yInner = y - topLeftY
      isInner = xInner >= 0 && xInner < width && yInner >= 0 && yInner < height
      originalPixel = another.pixels(another.width * y + x)
    } yield
      if (isInner) blendMode.blend(originalPixel, pixels(width * yInner + xInner))
      else originalPixel

    new Image(another.width, another.height, newColors.toArray)
  }
}

object Image {
  def apply(path: String): Image = new Image(path)
  def linearGradient(width: Int, height: Int, initialColor: Pixel, endColor: Pixel, vertical: Boolean = true): Image =
    if (vertical) verticalGradient(width, height, initialColor, endColor)
    else horizontalGradient(width, height, initialColor, endColor)

  def verticalGradient(width: Int, height: Int, initialColor: Pixel, endColor: Pixel): Image = {
    val initialRed = initialColor.red
    val initialGreen = initialColor.green
    val initialBlue = initialColor.blue
    val endRed = endColor.red
    val endGreen = endColor.green
    val endBlue = endColor.blue

    def intermediateColor(y: Int): Int = {
      val red = initialRed + (endRed - initialRed) * y / height
      val green = initialGreen + (endGreen - initialGreen) * y / height
      val blue = initialBlue + (endBlue - initialBlue) * y / height
      (red << 16) | (green << 8) | blue
    }

    val colors = for {
      y <- 0 until height
      _ <- 0 until width
      color = intermediateColor(y)
    } yield color

    new Image(width, height, colors.toArray)
  }

  // TODO this seems duplicated - perhaps we could do a .rotate at the expense of some perf?
  def horizontalGradient(width: Int, height: Int, initialColor: Pixel, endColor: Pixel): Image = {
    val initialRed = initialColor.red
    val initialGreen = initialColor.green
    val initialBlue = initialColor.blue
    val endRed = endColor.red
    val endGreen = endColor.green
    val endBlue = endColor.blue

    def intermediateColor(x: Int): Int = {
      val red = initialRed + (endRed - initialRed) * x / width
      val green = initialGreen + (endGreen - initialGreen) * x / width
      val blue = initialBlue + (endBlue - initialBlue) * x / width
      (red << 16) | (green << 8) | blue
    }

    val colors = for {
      _ <- 0 until height
      x <- 0 until width
      color = intermediateColor(x)
    } yield color

    new Image(width, height, colors.toArray)
  }

  val IMAGE_TYPE = 5 // RGB for now
}

object ImageTest {
  def testFilter(): Unit = {
    val image = Image("src/main/resources/daniel mugshot.jpg")
    val newImage = image.apply(List(Kernel.sobel1, Kernel.sobel2))
    newImage.write("src/main/resources/edge.jpg")
  }

  def testGradient(): Unit = {
    val image = Image.horizontalGradient(800, 600, Pixel.RED, Pixel.BLUE)
    image.write("src/main/resources/red-to-blue.jpg")
  }

  def testMirror(): Unit = {
    val image = Image("src/main/resources/red-to-blue.jpg")
    val mirrored = image.mirrorHorizontal()
    mirrored.write("src/main/resources/blue-to-red.jpg")
  }

  def testRotate(): Unit = {
    val image = Image("src/main/resources/paris.jpeg")
    val rotated = image.rotateLeft()
    rotated.write("src/main/resources/paris-rotated.jpg")
  }

  def testCrop(): Unit = {
    val image = Image("src/main/resources/paris.jpeg")
    val rotated = image.crop(0,0, 500, 500)
    rotated.write("src/main/resources/paris-cropped.jpg")
  }

  def testStack(): Unit = {
    val image = Image("src/main/resources/paris.jpeg")
    val daniel = Image("src/main/resources/daniel mugshot.jpg")
    val stack = daniel.onTopOf(image, 200, 200)
    stack.write("src/main/resources/stack.jpg")
  }

  def testBlend(): Unit = {
    val image = Image("src/main/resources/paris.jpeg")
    val daniel = Image("src/main/resources/daniel mugshot.jpg")
    val stack = daniel.blend(image, 200, 200, Transparency(0.5))
    stack.write("src/main/resources/transparency.jpg")
  }

  def testMultiply(): Unit = {
    val bg = Image("src/main/resources/db.png")
    val fg = Image("src/main/resources/fiery-lava.png")
    val stack = fg.blend(bg, 0, 0, Multiply)
    stack.write("src/main/resources/multiply.jpg")
  }

  def main(args: Array[String]): Unit = {
    testMultiply()
  }

}
