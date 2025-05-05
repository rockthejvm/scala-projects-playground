package imageKernelFilters.v2

import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Image private (private val buffImage: BufferedImage) { // NEVER expose mutable state outside this class
  val width: Int = buffImage.getWidth
  val height: Int = buffImage.getHeight

  def getColor(x: Int, y: Int): Pixel =
    Pixel.fromHex(buffImage.getRGB(x, y))

  def setColor(x: Int, y: Int, color: Pixel): Unit =
    buffImage.setRGB(x, y, color.toInt)

  def draw(g: Graphics): Unit = {
    g.drawImage(buffImage, 0, 0, null)
  }

  def save(path: String): Unit =
    ImageIO.write(buffImage, "JPG", new File(path))

  def saveResource(path: String): Unit =
    save(s"src/main/resources/$path")

  def crop(startX: Int, startY: Int, w: Int, h: Int): Image =
    if (
      startX < 0 || startX >= width || startY < 0 || startY >= height ||
        w < 0 || startX + w >= width || h < 0 || startY + h >= height
    ) {
      throw new RuntimeException("invalid coordinates or dimensions")
    } else {
      // happy path
      val result = Image.black(w, h)
      for (x <- startX until (startX + w))
        for (y <- startY until (startY + h)) {
          val originalPixel = buffImage.getRGB(x, y)
          result.buffImage.setRGB(x - startX, y - startY, originalPixel)
        }

      result
    }

  /*
     +-------------------------------------------------------+
     |                                                       |
     |           a1 a2 a3 a4 a5                              |
     |           b1 b2 b3 b4 b5                              |
  y >|           c1 c2 XX c4 c5                              |
     |           d1 d2 d3 d4 d5                              |
     |           e1 e2 e3 e4 e5                              |
     |                                                       |
     |                                                       |
     |                                                       |
     |                                                       |
     +-------------------------------------------------------+
                        ^
                        x
     Window(5,5, [a1,a2,a3,a4,a5, b1,b2,b3,b4,b5, c1,c2,XX,c4,c5, d1,d2,d3,d4,d5, e1,e2,e3,e4,e5])
   */
  def window(xc: Int, yc: Int, width: Int, height: Int): Window = {
    val offsetX = (width - 1) / 2
    val offsetY = (height - 1) / 2
    val horizCoords = ((xc - offsetX) to (xc + offsetX))
      .map { x =>
        if (x <= 0) 0
        else if (x >= this.width) this.width - 1
        else x
      }
    val vertCoords = ((yc - offsetY) to (yc + offsetY))
      .map { y =>
        if (y <= 0) 0
        else if (y >= this.height) this.height - 1
        else y
      }
    val colors = vertCoords
      .flatMap { y =>
        horizCoords.map { x => getColor(x, y) }
      }
    Window(width, height, colors.toList)
  }
}

object Image {
  def black(width: Int, height: Int): Image = {
    val buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val pixels = Array.fill(width * height)(0)
    buffImage.setRGB(0, 0, width, height, pixels, 0, width)
    new Image(buffImage)
  }

  def fromColors(width: Int, height: Int, colors: Seq[Pixel]): Image = {
    val buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val pixels = colors.map(_.toInt).toArray
    buffImage.setRGB(0, 0, width, height, pixels, 0, width)
    new Image(buffImage)
  }

  def apply(path: String): Image =
    new Image(ImageIO.read(new File(path)))

  def loadResource(path: String): Image =
    apply(s"src/main/resources/$path")
}

object ImagePlayground {
  def main(args: Array[String]): Unit = {
    Image.loadResource("wikicrop.jpg").crop(480, 180, 350, 180).saveResource("cropped.jpg")
  }
}