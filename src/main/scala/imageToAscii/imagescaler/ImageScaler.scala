package imageToAscii.imagescaler

import java.awt.RenderingHints
import java.awt.image.BufferedImage


trait ImageScaler {

  val maxSensibleWidth: Int
  val maxSensibleHeight: Int

  def scale(image: BufferedImage, widthSetting: Option[Int]): BufferedImage = {
    val (width, height) = chooseDimensions(image, widthSetting)
    val scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val gfx = scaledImage.createGraphics()
    gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    gfx.drawImage(image, 0, 0, width, height, null)
    gfx.dispose()
    scaledImage
  }

  private def chooseDimensions(image: BufferedImage, widthSetting: Option[Int]): (Int, Int) = {
    widthSetting match {
      case Some(width) =>
        // use specified width, keep aspect ratio the same
        val height = calcResizedHeight(image, width)
        (width, height)

      case None =>
        if (image.getWidth <= maxSensibleWidth && image.getHeight <= maxSensibleHeight) {
          // never enlarge the original image
          (image.getWidth, image.getHeight)
        } else if (image.getWidth > maxSensibleWidth && image.getHeight > maxSensibleHeight) {
          // give up, just make it as big as possible
          (maxSensibleWidth, maxSensibleHeight)
        } else if (image.getWidth > maxSensibleWidth) {
          // width too big & height ok, need to reduce both
          val tryHeight = calcResizedHeight(image, maxSensibleWidth)
          (maxSensibleWidth, tryHeight)
        } else {
          // height too big & width ok, need to reduce both
          val tryWidth = calcResizedWidth(image, maxSensibleHeight)
          (tryWidth, maxSensibleHeight)
        }
    }
  }

  private def calcResizedHeight(image: BufferedImage, resizedWidth: Int): Int =
    ((resizedWidth.toDouble / image.getWidth) * image.getHeight).toInt
  
  private def calcResizedWidth(image: BufferedImage, resizedHeight: Int): Int =
    ((resizedHeight.toDouble / image.getHeight) * image.getWidth).toInt
}
