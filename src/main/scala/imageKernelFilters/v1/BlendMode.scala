package imageKernelFilters.v1

import imageKernelFilters.v1.Pixel

sealed abstract class BlendMode {
  // TODO refactor perhaps need a color abstraction
  def blend(bg: Int, fg: Int): Int
}

case class Transparency(factor: Double) extends BlendMode {
  assert(factor >= 0 && factor <= 1, "transparency must be between 0 and 1")

  override def blend(bg: Int, fg: Int) = {
    val bgPixel = new Pixel(bg)
    val fgPixel = new Pixel(fg) //  * factor + Pixel.WHITE * (1 - factor)) * 0.5
    val result = bgPixel * (1 - factor) + fgPixel * factor
    result.toInt
  }
}

case object Multiply extends BlendMode {
  override def blend(bg: Int, fg: Int) = {
    val bgPixel = new Pixel(bg)
    val fgPixel = new Pixel(fg)
    val result = bgPixel * fgPixel
    result.toInt
  }
}

// maybe remove this one, it's harder to demo
case object ColorBurn extends BlendMode {
  override def blend(bg: Int, fg: Int) = {
    val bgPixel = new Pixel(bg)
    val fgPixel = new Pixel(fg)
    val result = Pixel.WHITE - (Pixel.WHITE - bgPixel) / fgPixel
    result.toInt
  }
}
