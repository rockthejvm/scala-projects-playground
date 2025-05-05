package imageToAscii.asciifier

import java.awt.Color
import java.awt.image.BufferedImage

trait Asciifier {
  import Asciifier._

  // to-be-implemented
  def asciify(image: BufferedImage): String

  // default methods
  def chooseChar(rgbMax: Double) =
    if (rgbMax == 0) asciiChars.last
    else {
      val index = ((asciiChars.length * (rgbMax / 255)) - 0.5).toInt // TODO figure this out, seems backwards
      asciiChars(index)
    }

  def rgbMax(pixel: Color) =
    List(pixel.getRed, pixel.getGreen, pixel.getBlue).reduceLeft(Math.max)

  def mapImage[A](image: BufferedImage)(f: Color => A): Seq[Seq[A]] = {
    (0 until image.getHeight) map { y =>
      (0 until image.getWidth) map { x =>
        val pixel = new Color(image.getRGB(x, y))
        f(pixel)
      }
    }
  }
}

object Asciifier {
  val asciiChars = List('#','A','@','%','$','+','=','*',':',',','.',' ')
}

