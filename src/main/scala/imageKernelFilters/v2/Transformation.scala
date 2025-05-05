package imageKernelFilters.v2

import java.io.IOException

trait Transformation {
  def apply(image: Image): Image
}

object Transformation {
  def parse(string: String): Transformation = {
    val words = string.split(" ")
    val command = words(0)
    command match {
      case "crop" =>
        try {
          Crop(
            words(1).toInt,
            words(2).toInt,
            words(3).toInt,
            words(4).toInt
          )
        } catch {
          case _: Exception =>
            println("Invalid crop format. Usage: 'crop [x] [y] [w] [h]'")
            Noop
        }
      case "blend" =>
        try {
          Blend(
            Image.loadResource(words(1)),
            BlendMode.parse(words(2))
          )
        } catch {
          case _: IOException =>
            println("Invalid image.")
            Noop
          case _: Exception =>
            println("Invalid blend format. Usage: 'blend [path] [mode]'")
            Noop
        }
      case "invert" => Invert
      case "grayscale" => Grayscale
      case "sharpen" => KernelFilter(Kernel.sharpen)
      case "blur" => KernelFilter(Kernel.blur)
      case "edge" => KernelFilter(Kernel.edge)
      case "emboss" => KernelFilter(Kernel.emboss)
      case _ => Noop
    }
  }
}

case class Crop(x: Int, y: Int, w: Int, h: Int) extends Transformation {
  override def apply(image: Image): Image =
    try {
      image.crop(x, y, w, h) // will crash if the coords are out of bounds
    } catch {
      case _: Exception =>
        println(s"Error: coordinates are out of bounds. Max coordinates: ${image.width} x ${image.height}")
        image
    }
}

case class Blend(fgImage: Image, mode: BlendMode) extends Transformation {
  override def apply(bgImage: Image): Image = {
    // 1
    if (fgImage.width != bgImage.width || fgImage.height != bgImage.height) {
      println(s"Error: images don't have the same sizes: ${fgImage.width} x ${fgImage.height} vs ${bgImage.width} x ${bgImage.height}")
      return bgImage
    }

    val width = fgImage.width
    val height = fgImage.height
    // 2
    val result = Image.black(width, height)
    // 3
    for (x <- 0 until width)
      for (y <- 0 until height)
        result.setColor(
          x,
          y,
          mode.blend(
            fgImage.getColor(x, y),
            bgImage.getColor(x, y)
          )
        )
    // 4
    result
  }
}

abstract class PixelTransformation(pixelFun: Pixel => Pixel) extends Transformation {
  override def apply(image: Image): Image = {
    val width = image.width
    val height = image.height
    val result = Image.black(width, height)
    for (x <- 0 until width)
      for (y <- 0 until height) {
        val originalColor = image.getColor(x, y)
        val newColor = pixelFun(originalColor)
        result.setColor(x, y, newColor)
      }

    result
  }
}

object Invert extends PixelTransformation(color =>
  Pixel(
    255 - color.red,
    255 - color.green,
    255 - color.blue
  )
)

object Grayscale extends PixelTransformation(color => {
  val avg = (color.red + color.green + color.blue) / 3
  Pixel(avg, avg, avg) // last expression is the value of the lambda
})

object Noop extends Transformation {
  override def apply(image: Image): Image = image
}

// kernel transformation
case class Window(width: Int, height: Int, values: List[Pixel])
case class Kernel(width: Int, height: Int, values: List[Double]) {
  // property: all the values should sum up to 1.0

  def normalize(): Kernel = {
    val sum = values.sum
    if (sum == 0.0) return this
    Kernel(width, height, values.map(_ / sum))
  }

  // window and kernel must have the same width x height
  // multiply every pixel with every CORRESPONDING double
  // [a,b,c] * [x,y,z] = [a * x, b * y, c * z]
  // sum up all the values to a single color = a * x + b * y + c * z
  // "convolution"
  def *(window: Window): Pixel = {
    if (width != window.width || height != window.height)
      throw new IllegalArgumentException("Kernel and window must have the same dimensions")

    val r = window.values
      .map(_.red)
      .zip(values)
      .map { case (a, b) => a * b }
      .sum
      .toInt
    val g = window.values
      .map(_.green)
      .zip(values)
      .map { case (a, b) => a * b }
      .sum
      .toInt
    val b = window.values
      .map(_.blue)
      .zip(values)
      .map { case (a, b) => a * b }
      .sum
      .toInt

    Pixel(r, g, b)
  }
}

object Kernel {
  val sharpen = Kernel(3, 3, List(
    0.0, -1.0, 0.0,
    -1.0, 5.0, -1.0,
    0.0, -1.0, 0.0
  )).normalize()

  val blur = Kernel(3, 3, List(
    1.0, 2.0, 1.0,
    2.0, 4.0, 2.0,
    1.0, 2.0, 1.0
  )).normalize()

  val edge = Kernel(3, 3, List(
    1.0, 0.0, -1.0,
    2.0, 0.0, -2.0,
    1.0, 0.0, -1.0
  ))

  val emboss = Kernel(3, 3, List(
    -2.0, -1.0, 0.0,
    -1.0, 1.0, 1.0,
    0.0, 1.0, 2.0
  ))
}

case class KernelFilter(kernel: Kernel) extends Transformation {
  override def apply(image: Image): Image =
    Image.fromColors(
      image.width,
      image.height,
      (0 until image.height).flatMap(y =>
        (0 until image.width).map(x =>
          kernel * image.window(x, y, kernel.width, kernel.height)
        )
      )
    )
}