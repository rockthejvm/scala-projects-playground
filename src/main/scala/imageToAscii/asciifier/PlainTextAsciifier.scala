package imageToAscii.asciifier

import java.awt.Color
import java.awt.image.BufferedImage

class PlainTextAsciifier extends Asciifier  {
//  val maxSensibleWidth = 120
//  val maxSensibleHeight = 50

  def asciify(image: BufferedImage) = {
    val lines: Seq[Seq[(Char, Color)]] = mapImage(image) { pixel =>
      val char = chooseChar(rgbMax(pixel))
      (char, pixel)
    }
    lines.map(_.map(_._1).mkString(" ")).mkString("\n")
    //    lines.map(line => "        " + Rainbow.rainbowify(line, escape = false)).mkString("\n")
  }

}