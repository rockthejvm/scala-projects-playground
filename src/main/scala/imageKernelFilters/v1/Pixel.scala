package imageKernelFilters.v1

import java.awt.image.BufferedImage

// all values will be in [0, 1]
// TODO perhaps name "Color"?
case class Pixel(r: Double, g: Double, b: Double) {

  // We'll throw an exception any time we want to build a OldPixel object with invalid numbers.
  if(r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1)
    throw new IllegalArgumentException(s"Colors should be between 0 and 1. Got $r, $g, $b")

  // We'll compute the absolute red, green and blue channels here for convenience.
  val red = Math.round(r * 255).toInt
  val green = Math.round(g * 255).toInt
  val blue = Math.round(b * 255).toInt

  def this(red: Int, green: Int, blue: Int) =
    this(red * 1.0 / 255, green * 1.0 / 255, blue * 1.0 / 255)

  def this(color: Int) =
    this(
      (color & 0xff0000) >> 16,
      (color & 0xff00) >> 8,
      color & 0xff
    )
  /*
      This function will rebuild the OldPixel value as a simple Int.
      We do that by setting the right bits for each channel.
      If you don't know how to work with bits, don't worry, that's not our focus here.
  */
  def toInt: Int = (red << 16) | (green << 8) | blue

  override def toString: String = s"($red, $green, $blue)"

  def displayColor(width: Int, height: Int, path: String): Unit = {
    val newImage = new BufferedImage(width, height, Image.IMAGE_TYPE)
    val values: Array[Int] = Array.fill(width * height)(toInt)
    newImage.setRGB(0, 0, width, height, values, 0, width)
  }

  def displayColor(): Unit = displayColor(100, 100, "src/main/resources/OldPixelTest.jpg")

  def negative(): Pixel = Pixel(1-r, 1-g, 1-b)

  def gray(): Pixel = {
    val avg = (r + g + b) / 3
    Pixel(avg, avg, avg)
  }

  def +(other: Pixel): Pixel = {
    // YOUR CODE HERE (1-4 lines)
    val newr = Math.min(r + other.r, 1)
    val newg = Math.min(g + other.g, 1)
    val newb = Math.min(b + other.b, 1)
    Pixel(newr, newg, newb)
  }

  def -(other: Pixel): Pixel = {
    // YOUR CODE HERE (1-4 lines)
    val newr = Math.max(r - other.r, 0)
    val newg = Math.max(g - other.g, 0)
    val newb = Math.max(b - other.b, 0)
    Pixel(newr, newg, newb)
  }

  def *(other: Pixel): Pixel = {
    // YOUR CODE HERE (~1 line)
    Pixel(r * other.r, g * other.g, b * other.b)
  }

  def *(scalar: Double): Pixel = {
    val newr = Math.max(0, Math.min(1, r * scalar)) // clamp between 0, 1
    val newg = Math.max(0, Math.min(1, g * scalar))
    val newb = Math.max(0, Math.min(1, b * scalar))
    Pixel(newr, newg, newb)
  }

  def /(other: Pixel): Pixel = {
    val newr = Math.max(0, Math.min(1, r / other.r))
    val newg = Math.max(0, Math.min(1, g / other.g))
    val newb = Math.max(0, Math.min(1, b / other.b))
    Pixel(newr, newg, newb)

  }

  def /(scalar: Double): Pixel = *(1/scalar)
}

/*
    Companion object for ease of OldPixel construction.
    Have a look over the apply() methods - they will come in handy.

    Don't worry about the implementation, though - we work with bits to extract the channel values.
*/
object Pixel {
  val MASK_BLUE = 0xFF
  val MASK_GREEN = MASK_BLUE << 8
  val MASK_RED = MASK_BLUE << 16
  val MASK_ALPHA = MASK_BLUE << 24

  /*
      Handy apply() method that takes a plain `Int` value (as described earlier)
      and converts it to a OldPixel by extracting the appropriate channel values.
  */
  def apply(value: Int): Pixel = {
    val red = (value & MASK_RED) >> 16
    val green = (value & MASK_GREEN) >> 8
    val blue = value & MASK_BLUE

    new Pixel(red, green, blue)
  }

  def clamp(r: Double, g: Double, b: Double) = Pixel(
    if (r <= 0) 0 else if (r >= 1) 1 else r,
    if (g <= 0) 0 else if (g >= 1) 1 else g,
    if (b <= 0) 0 else if (b >= 1) 1 else b
  )

  val BLACK = Pixel(0,0,0)
  val WHITE = Pixel(1,1,1)
  val RED = Pixel(1,0,0)
  val GREEN = Pixel(0,1,0)
  val BLUE = Pixel(0,0,1)
}

/*
    TEST CODE

    Uncomment these tests one by one to see the results of your work.
    Feel free to try other combinations as well!
*/

// // expected result: a darker turquoise
// val p = new OldPixel(0,128,160)
// p.displayColor

// // expected: a soft shade of orange
// val negative = p.negative
// negative.displayColor

// // expected: a somewhat dark gray
// val gray = p.gray
// gray.displayColor

// // a darker purple
// val q = new OldPixel(100, 0, 150)
// q.displayColor

// // a shade of green, not dark, not bright
// (p - q).displayColor

// // the kind of blue from the crack of dawn on a semi-cloudy day
// (p + q).displayColor

// // white - should be invisible on the notebook
// (p + negative).displayColor

// // a very, very dark blue
// (p * q).displayColor
