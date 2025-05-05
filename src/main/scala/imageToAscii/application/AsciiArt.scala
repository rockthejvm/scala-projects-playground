package imageToAscii.application

import imageToAscii.asciifier.{Asciifier, HtmlAsciifier, PlainTextAsciifier}
import imageToAscii.imageloader.{FileLoader, ImageLoader}
import imageToAscii.imagescaler.{ImageScaler, PlainTextScaler}

trait AsciiArtApplication {
  val asciifier: Asciifier
  val imageLoader: ImageLoader
  val imageScaler: ImageScaler
}

object AsciiArt extends AsciiArtApplication {

  override val asciifier = new HtmlAsciifier
  override val imageLoader = new FileLoader
  override val imageScaler = new PlainTextScaler

  def main(args: Array[String]): Unit = {
    // can read the file path from the command line
    val path = "src/main/resources/fiery-lava.png"
    val width = Some(100)

    val imageOption = imageLoader.loadImage(path)
    val scaledImageOption = imageOption.map(image => imageScaler.scale(image, width))
    val asciiImageOption = scaledImageOption.map(image => asciifier.asciify(image))

    asciiImageOption.foreach(println)
  }
}


