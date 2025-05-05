package imageKernelFilters.v2

trait BlendMode {
  def blend(fg: Pixel, bg: Pixel): Pixel
}

object BlendMode {
  def parse(string: String): BlendMode = string match {
    case "transparency" => Transparency(0.5)
    case "multiply" => Multiply
    case "screen" => Screen
    case _ => NoBlend
  }
}

case class Transparency(f: Double) extends BlendMode {
  val factor: Double =
    if (f <= 0.0) 0.0
    else if (f >= 1.0) 1.0
    else f

  override def blend(fg: Pixel, bg: Pixel): Pixel =
    Pixel(
      (fg.red * factor + bg.red * (1 - factor)).toInt,
      (fg.green * factor + bg.green * (1 - factor)).toInt,
      (fg.blue * factor + bg.blue * (1 - factor)).toInt
    )
}

object Screen extends BlendMode {
  override def blend(fg: Pixel, bg: Pixel): Pixel =
    // result = 255 - (255 - fg) * (255 - bg) / 255
    Pixel(
      (255 - (255 - fg.red) * (255 - bg.red) / 255.0).toInt,
      (255 - (255 - fg.green) * (255 - bg.green) / 255.0).toInt,
      (255 - (255 - fg.blue) * (255 - bg.blue) / 255.0).toInt
    )
}

object Multiply extends BlendMode {
  override def blend(fg: Pixel, bg: Pixel): Pixel =
    Pixel(
      (fg.red * bg.red / 255.0).toInt,
      (fg.green * bg.green / 255.0).toInt,
      (fg.blue * bg.blue / 255.0).toInt
    )
}

object NoBlend extends BlendMode {
  override def blend(fg: Pixel, bg: Pixel): Pixel = fg
}

object BlendPlayground {
  def main(args: Array[String]): Unit = {
    // Transparency(0.5).blend(Pixel.RED, Pixel.BLUE).draw(100, 100, "src/main/resources/darkmagenta.jpg")
    // Multiply.blend(Pixel.RED, Pixel.GRAY).draw(100, 100, "src/main/resources/darkred.jpg")
    // Multiply.blend(Pixel.RED, Pixel.BLUE).draw(100, 100, "src/main/resources/black.jpg")
    BlendMode.parse("screen").blend(Pixel.RED, Pixel.GRAY).draw(100, 100, "src/main/resources/lightred.jpg")
  }
}
