package imageKernelFilters.v2

import java.awt.{Dimension, Graphics}
import java.util.Scanner
import javax.swing.{JFrame, JPanel, WindowConstants}

// Java Swing
object App {
  private var frame: Option[JFrame] = None
  private var imagePanel: Option[ImagePanel] = None

  class ImagePanel(private var image: Image) extends JPanel {
    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      // render the picture inside this "graphics"
      image.draw(g)
    }

    override def getPreferredSize: Dimension =
      new Dimension(image.width, image.height)

    def replaceImage(newImage: Image): Unit = {
      image = newImage
      revalidate()
      repaint()
    }

    def getImage: Image = image
  }

  def loadResource(path: String): Unit = {
    val image = Image.loadResource(path)
    if (frame.isEmpty) {
      val newFrame = new JFrame("Scala Image App")
      val newImagePanel = new ImagePanel(image)

      newFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      newFrame.getContentPane.add(newImagePanel)
      newFrame.pack()
      newFrame.setVisible(true)

      frame = Some(newFrame)
      imagePanel = Some(newImagePanel)
    } else {
      imagePanel.foreach { panel =>
        panel.replaceImage(image)
        frame.foreach(_.pack()) // resizes the window to the "preferred dimensions"
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val scanner = new Scanner(System.in)
    while (true) {
      print("> ")
      val command = scanner.nextLine()
      val words = command.split(" ")
      val action = words(0)
      action match {
        case "load" =>
          try {
            loadResource(words(1))
          } catch {
            case _: Exception =>
              println(s"Error: cannot load image at path ${words(1)}.")
          }
        case "save" =>
          if (frame.isEmpty)
            println("Error: No image loaded.")
          else
            imagePanel.foreach(_.getImage.saveResource(words(1)))
        case "exit" => System.exit(0)
        case _ =>
          if (frame.isEmpty)
            println("Error: Must have an image loaded before running any transformation.")
          else {
            val transformation = Transformation.parse(command)
            imagePanel.foreach { panel =>
              val newImage = transformation(panel.getImage)
              panel.replaceImage(newImage)
              frame.foreach(_.pack())
            }
          }
      }
    }
  }
}