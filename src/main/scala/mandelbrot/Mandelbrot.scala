package mandelbrot

import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO

abstract class ColorScheme {
  def color(iter: Int, threshold: Int): Int
}

object NormalColorScheme extends ColorScheme {
  override def color(iters: Int, threshold: Int) = {
    val a = 0xff << 24
    val r = Math.min(255, 1.0 * iters / threshold * 255).toInt << 16
    val g = Math.min(255, 2.0 * iters / threshold * 255).toInt << 8
    val b = Math.min(255, 3.0 * iters / threshold * 255).toInt << 0
    a | r | g | b
  }
}

class TwoTone(start: Int, end: Int) extends ColorScheme {
  val rStart = (start & 0xFF0000) >> 16
  val gStart = (start & 0xFF00) >> 8
  val bStart = start & 0xFF
  val rEnd = (end & 0xFF0000) >> 16
  val gEnd = (end & 0xFF00) >> 8
  val bEnd = end & 0xFF

  override def color(iters: Int, threshold: Int) = {
    val a = 0xFF << 24
    val r = Math.min(255, rStart + (rEnd - rStart) * 1.0 * iters / threshold).toInt << 16
    val g = Math.min(255, gStart + (gEnd - gStart) * 1.0 * iters / threshold).toInt << 8
    val b = Math.min(255, bStart + (bEnd - bStart) * 1.0 * iters / threshold).toInt
    a | r | g | b
  }
}

object Grayscale extends TwoTone(0, 0xffffff)
object Sepia extends TwoTone(0xb13700, 0xffffff)

class MultitoneGradient(colors: List[(Int, Double)]) extends ColorScheme {
  override def color(iter: Int, threshold: Int) = {
    val position = 1.0 * iter / threshold
    val startPair = colors.filter(_._2 <= position).maxBy(_._2)
    val endPair = colors.filter(_._2 >= position).minBy(_._2)
    val start = startPair._1
    val end = endPair._1
    val startPosition = startPair._2
    val endPosition = endPair._2

    val rStart = (start & 0xFF0000) >> 16
    val gStart = (start & 0xFF00) >> 8
    val bStart = start & 0xFF
    val rEnd = (end & 0xFF0000) >> 16
    val gEnd = (end & 0xFF00) >> 8
    val bEnd = end & 0xFF

    val a = 0xFF << 24
    val r = Math.min(255, rStart + (rEnd - rStart) * (position - startPosition) / (endPosition - startPosition)).toInt << 16
    val g = Math.min(255, gStart + (gEnd - gStart) * (position - startPosition) / (endPosition - startPosition)).toInt << 8
    val b = Math.min(255, bStart + (bEnd - bStart) * (position - startPosition) / (endPosition - startPosition)).toInt
    a | r | g | b

  }
}

object Fire extends MultitoneGradient(List(
  (0,0),
  (0xff6000, 0.35),
  (0xffffff, 1)
))

object Golden extends MultitoneGradient(List(
  (0,0),
  (0x2d0039, 0.05),
  (0xffba00, 0.45),
  (0xffffff, 1)
))

object Rainbow extends MultitoneGradient(List(
  (0,0),
  (0xFF0000, 0.125),
  (0xFF7F00, 0.25),
  (0xFFFF00, 0.375),
  (0x00FF00, 0.5),
  (0x0000FF, 0.625),
  (0x2E2B5F, 0.75),
  (0x8B00FF, 0.875),
  (0xFFFFFF, 1)
))

object Zerg extends MultitoneGradient(List(
  (0,0),
  (0xc65100, 0.1),
  (0x8e35a6, 0.3),
  (0xffffff, 0.9),
  (0xffffff, 1)
))

object MandelbrotStock {

  val defaultThreshold = 2000 /* TODO see if this threshold is appropriate */
  val leftLimit = -2
  val rightLimit = 0.6
  val colorScheme = NormalColorScheme

  def scaleCoords(xCenter: Double, yCenter: Double, zoom: Double, aspectRatio: Double): (Double, Double, Double, Double) = {
    val originalInterval = rightLimit - leftLimit
    val newInterval = originalInterval / zoom
    val verticalInterval = newInterval / aspectRatio
    val xLeft = xCenter - newInterval / 2
    val xRight = xCenter + newInterval / 2
    val yLo = yCenter - verticalInterval / 2
    val yHi = yCenter + verticalInterval / 2

    (xLeft, xRight, yLo, yHi)
  }

  // number of iterations to determine whether a point (xc, yc) is divergent or not
  def compute(xc: Double, yc: Double, threshold: Int): Int = {
    def computeIter() = {
      var i = 0
      var x = 0.0
      var y = 0.0
      while (x * x + y * y < 2 && i < threshold) {
        val xt = x * x - y * y + xc
        val yt = 2 * x * y + yc

        x = xt
        y = yt

        i += 1
      }
      i
    }

    def computeRec(i: Int = 0, x: Double = 0, y: Double = 0): Int =
      if (x * x + y * y < 2 && i < threshold) {
        val xt = x * x - y * y + xc
        val yt = 2 * x * y + yc
        computeRec(i + 1, xt, yt)
      } else i

    computeRec()
  }

  def colorPixels(xlo: Double, xhi: Double, ylo: Double, yhi: Double, wdt: Int, hgt: Int, threshold: Int): Array[Int] = {
    // arrays are mutable
    val pixels = Array.ofDim[Int](wdt * hgt)

    (0 until (wdt * hgt)).foreach { idx =>
      val x = idx % wdt
      val y = idx / wdt
      val xc = xlo + (xhi - xlo) * x / wdt
      val yc = ylo + (yhi - ylo) * (hgt - y) / hgt // image is vertically flipped

      val iters = compute(xc, yc, threshold)
      val color = colorScheme.color(iters, threshold)
      pixels(idx) = color
    }

    pixels
  }

  // parallelize computations with Futures
  def colorPixelsPar(xlo: Double, xhi: Double, ylo: Double, yhi: Double, wdt: Int, hgt: Int, threshold: Int): Array[Int] = {
    import scala.concurrent.*
    import scala.concurrent.duration.*
    import java.util.concurrent.ForkJoinPool

    val forkJoinPool = new ForkJoinPool(Runtime.getRuntime.availableProcessors)
    given ExecutionContext = ExecutionContext.fromExecutor(forkJoinPool)

    // arrays are mutable
    val pixels = Array.ofDim[Int](wdt * hgt)

    val futures = (0 until (wdt * hgt)).map { idx =>
      Future {
        val x = idx % wdt
        val y = idx / wdt
        val xc = xlo + (xhi - xlo) * x / wdt
        val yc = ylo + (yhi - ylo) * (hgt - y) / hgt // image is vertically flipped

        val iters = compute(xc, yc, threshold)
        val color = colorScheme.color(iters, threshold)
        pixels(idx) = color
      }
    }

    Await.result(Future.sequence(futures), 1.minute)
    pixels
  }

  def writePic(width: Int, height: Int, pixels: Array[Int], path: String): Unit = {
    val bfImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    bfImg.setRGB(0, 0, width, height, pixels, 0, width)
    ImageIO.write(bfImg, "JPG", new File(path))
  }

  def createPicture(xlo: Double, xhi: Double, ylo: Double, yhi: Double, width: Int, height: Int, nIterations: Int, path: String): Unit = {
    val pixels = colorPixelsPar(xlo, xhi, ylo, yhi, width, height, nIterations)
    writePic(width, height, pixels, path)
  }

  def drawFromCenter(x: Double, y: Double, zoom: Long, path: String): Unit = {
    val (xLo, xHi, yLo, yHi) = scaleCoords(x, y, zoom, 1)
    val nIterations = Math.round(200 * Math.log1p(zoom) / Math.log(2)).toInt
    println(s"Nr iterations: $nIterations")
    createPicture(xLo, xHi, yLo, yHi, 2000, 2000, nIterations, path)
  }

  def createZoomSequence(nPics: Int, xCenter: Double, yCenter: Double, folderPath: String, picIndexStart: Int = 0): Unit = {
    val zoomLevels = (1 to nPics).map(_.toLong).scan(Math.pow(4, picIndexStart).toLong)((zoom, _) => zoom * 4L)
    zoomLevels.zipWithIndex.foreach {
      case (zoom, index) =>
        println(s"Drawing image ${index + picIndexStart}, zoom ${zoom}x")
        drawFromCenter(xCenter, yCenter, zoom, folderPath + s"/${index + picIndexStart}.jpg")
    }
  }

  def getPixels(xlo: Double, xhi: Double, ylo: Double, yhi: Double, width: Int, height: Int, nIterations: Int, scheme: ColorScheme = colorScheme): Array[Int] = {
    colorPixelsPar(xlo, xhi, ylo, yhi, width, height, nIterations)
  }

  def main(args: Array[String]): Unit = {
    // (manual) zoom sequence
    // createPicture(-2, 0.6, -1.3, 1.3, 2000, 2000, 1000, "src/main/resources/mandelbrot/1-original.jpg")
    // createPicture(-0.922, -0.578, -0.086, 0.258, 2000, 2000, "src/main/resources/mandelbrot/2-valley.jpg")
    // createPicture(-0.761, -0.739, 0.075, 0.097, 2000, 2000, "src/main/resources/mandelbrot/3-spikes.jpg")
    // createPicture(-0.750, -0.745, 0.082, 0.087, 2000, 2000, 1000, "src/main/resources/mandelbrot/4-spike-v2.jpg")
    // drawFromCenter(-0.7487252541866029, 0.08451351300837322, 4506, "src/main/resources/mandelbrot/5-tentacle.jpg")
    // drawFromCenter(-0.748692918031617, 0.08466664514462811, 23108, "src/main/resources/mandelbrot/6-tentacle-arm.jpg")
    // drawFromCenter(-0.7487324142851152, 0.08465454262291397, 118517, "src/main/resources/mandelbrot/7-2-tentacles.jpg")

    // drawFromCenter(-0.743643887037158704752191506114774, 0.131825904205311970493132056385139, 1024 * 1024 * 8, "src/main/resources/mandelbrot/exp.jpg")
    // automatic zoom sequence
    // createZoomSequence(20, -0.743643887037158704752191506114774, 0.131825904205311970493132056385139, "src/main/resources/mandelbrot/zoom-small")

    // other interesting places
    // createPicture(-0.7092, -0.712, 0.24445, 0.2487, 2000, 2000, 1000, "src/main/resources/mandelbrot/deathspiral.jpg")

    // fire, gray
    // createPicture(-0.7092, -0.712, 0.24445, 0.2487, 2000, 2000, 1000, "src/main/resources/mandelbrot/deathspiral-fire.jpg")

    // golden:
    //  (0,0),
    //  (0x2d0039, 0.05),
    //  (0xffba00, 0.45),
    //  (0xffffff, 1)
    // createPicture(-0.7092, -0.712, 0.24445, 0.2487, 2000, 2000, 1000, "src/main/resources/mandelbrot/deathspiral-golden.jpg")

    // rainbow
    // drawFromCenter(-0.7487324142851152, 0.08465454262291397, 4118517, "src/main/resources/mandelbrot/mini-rainbow.jpg")
  }
}