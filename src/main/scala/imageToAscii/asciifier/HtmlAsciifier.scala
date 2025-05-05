package imageToAscii.asciifier

import java.awt.image.BufferedImage


class HtmlAsciifier extends Asciifier {

  def asciify(image: BufferedImage) = {
    val lines = mapImage(image) { pixel =>
      val char = chooseChar(rgbMax(pixel))
      charToSpan(pixel.getRed, pixel.getGreen, pixel.getBlue, char)
    }

    s"""
      <html>
        <body style="padding: 20px;">
          <p style="
            font-family:Courier,monospace;
            font-size:5pt;
            letter-spacing:1px;
            line-height:4pt;
            font-weight:bold">
            ${ lines.map(xml => xml.mkString("") ++ "<br/>").mkString("") }
          </p>
        </body>
      </html>
    """
  }

  def charToSpan(red: Int, green: Int, blue: Int, char: Char): String = {
    val string = toHtmlString(char)
    "<span style=\"" + String.format("display:inline; color: rgb(%s, %s, %s);", red.toString, green.toString, blue.toString) + "\">" + string + "</span>"
  }

  def toHtmlString(char: Char) = char match {
    case ' ' => "&nbsp;"
    case c => c.toString
  }
}