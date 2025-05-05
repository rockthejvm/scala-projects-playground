package mandelbrot

import javafx.application.{Application, Platform}
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control._
import javafx.scene.image.{PixelWriter, WritableImage}
import javafx.scene.layout.{BorderPane, HBox, VBox}
import javafx.stage.{FileChooser, Stage}
import mandelbrot._

import java.io.File
import javax.imageio.ImageIO
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/*
  Generate this with AI, change the code as necessary.
  Prompt: 
    Write a JavaFX application that uses the Mandelbrot code in this file and renders the set in real time. 
    Add zoom, pan and "save image" controls. 
    Include changes to build.sbt, and changes to the API of the MandelbrotStock object to be able to be called from the JavaFX application.
 */
// remove non-local returns, use idiomatic scala
class MandelbrotExplorer extends Application {

  private var canvas: Canvas = _
  private var xCenter: Double = -0.5
  private var yCenter: Double = 0
  private var zoom: Double = 1.0
  private var maxIterations: Int = 1000
  private var colorScheme: ColorScheme = NormalColorScheme
  private var rendering: Boolean = false
  private var lastX, lastY: Double = 0
  private var currentImage: WritableImage = _
  private var statusLabel: Label = _

  override def start(stage: Stage): Unit = {
    stage.setTitle("Mandelbrot Explorer")

    val root = new BorderPane()
    canvas = new Canvas(800, 800)

    // Add mouse event handlers for panning
    canvas.setOnMousePressed(e => {
      lastX = e.getX
      lastY = e.getY
    })

    canvas.setOnMouseDragged { e => 
      if (!rendering) {
        val dx = e.getX - lastX
        val dy = e.getY - lastY
  
        val aspectRatio = canvas.getWidth / canvas.getHeight
        val intervalWidth = 2.6 / zoom
        val pixelSize = intervalWidth / canvas.getWidth
  
        xCenter -= dx * pixelSize
        yCenter += dy * pixelSize // Inverted for screen coordinates
  
        lastX = e.getX
        lastY = e.getY
  
        renderMandelbrot()
      }
  }

    // Add mouse wheel for zooming
    canvas.setOnScroll { e =>
      if (!rendering) {

        val zoomFactor = if (e.getDeltaY > 0) 1.2 else 0.8

        // Zoom toward mouse position
        val mouseX = e.getX
        val mouseY = e.getY

        val aspectRatio = canvas.getWidth / canvas.getHeight
        val intervalWidth = 2.6 / zoom
        val pixelSize = intervalWidth / canvas.getWidth

        // Convert mouse position to complex plane coordinates
        val realPart = xCenter + (mouseX - canvas.getWidth / 2) * pixelSize
        val imagPart = yCenter - (mouseY - canvas.getHeight / 2) * pixelSize

        // Apply zoom
        zoom *= zoomFactor

        // Update maxIterations based on zoom level
        maxIterations = Math.max(1000, Math.round(200 * Math.log1p(zoom) / Math.log(2)).toInt)

        // Update center to keep mouse position stable
        xCenter = realPart
        yCenter = imagPart

        renderMandelbrot()
      }
    }

    root.setCenter(canvas)

    // Create toolbar
    val toolbar = new HBox(10)
    toolbar.setPadding(new Insets(10))

    // Color scheme selector
    val colorSchemeSelector = new ComboBox[String]()
    colorSchemeSelector.getItems.addAll(
      "Normal", "Grayscale", "Sepia", "Fire", "Golden", "Rainbow", "Zerg"
    )
    colorSchemeSelector.setValue("Normal")
    colorSchemeSelector.setOnAction(_ => {
      val selected = colorSchemeSelector.getValue
      colorScheme = selected match {
        case "Normal" => NormalColorScheme
        case "Grayscale" => Grayscale
        case "Sepia" => Sepia
        case "Fire" => Fire
        case "Golden" => Golden
        case "Rainbow" => Rainbow
        case "Zerg" => Zerg
      }
      renderMandelbrot()
    })

    // Reset button
    val resetButton = new Button("Reset View")
    resetButton.setOnAction(_ => {
      xCenter = -0.5
      yCenter = 0
      zoom = 1.0
      maxIterations = 1000
      renderMandelbrot()
    })

    // Save button
    val saveButton = new Button("Save Image")
    saveButton.setOnAction(_ => {
      if (currentImage == null) return

      val fileChooser = new FileChooser()
      fileChooser.setTitle("Save Mandelbrot Image")
      fileChooser.getExtensionFilters.add(
        new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg")
      )
      val file = fileChooser.showSaveDialog(stage)

      if (file != null) {
        try {
          val bufferedImage = SwingFXUtils.fromFXImage(currentImage, null)
          ImageIO.write(bufferedImage, "JPG", file)
          statusLabel.setText(s"Image saved to ${file.getName}")
        } catch {
          case ex: Exception => statusLabel.setText(s"Error saving image: ${ex.getMessage}")
        }
      }
    })

    // Status bar
    statusLabel = new Label("Ready")

    toolbar.getChildren.addAll(
      new Label("Color Scheme:"),
      colorSchemeSelector,
      resetButton,
      saveButton
    )

    val bottomBox = new VBox(10)
    bottomBox.setPadding(new Insets(10))
    bottomBox.getChildren.addAll(toolbar, statusLabel)

    root.setBottom(bottomBox)

    val scene = new Scene(root, 800, 900)
    stage.setScene(scene)
    stage.show()

    // Initial render
    renderMandelbrot()
  }

  private def renderMandelbrot(): Unit = {
    if (rendering) return

    rendering = true
    val width = canvas.getWidth.toInt
    val height = canvas.getHeight.toInt

    statusLabel.setText(s"Rendering... (Iterations: $maxIterations, Zoom: ${f"$zoom%.1f"}x)")

    val aspectRatio = width.toDouble / height
    val (xlo, xhi, ylo, yhi) = calculateCoordinates(xCenter, yCenter, zoom, aspectRatio)

    implicit val ec: ExecutionContext = ExecutionContext.global

    Future {
      MandelbrotStock.getPixels(xlo, xhi, ylo, yhi, width, height, maxIterations, colorScheme)
    }.onComplete {
      case Success(pixels) => Platform.runLater(() => {
        val image = new WritableImage(width, height)
        val pixelWriter = image.getPixelWriter

        for (y <- 0 until height; x <- 0 until width) {
          val argb = pixels(y * width + x)
          pixelWriter.setArgb(x, y, argb)
        }

        val gc = canvas.getGraphicsContext2D
        gc.drawImage(image, 0, 0)
        currentImage = image

        statusLabel.setText(f"Ready - Position: ($xCenter%.8f, $yCenter%.8f)")
        rendering = false
      })
      case Failure(exception) => Platform.runLater(() => {
        statusLabel.setText(s"Error: ${exception.getMessage}")
        rendering = false
      })
    }
  }

  private def calculateCoordinates(xCenter: Double, yCenter: Double, zoom: Double, aspectRatio: Double): (Double, Double, Double, Double) = {
    val originalInterval = MandelbrotStock.rightLimit - MandelbrotStock.leftLimit
    val newInterval = originalInterval / zoom
    val verticalInterval = newInterval / aspectRatio

    val xLeft = xCenter - newInterval / 2
    val xRight = xCenter + newInterval / 2
    val yLo = yCenter - verticalInterval / 2
    val yHi = yCenter + verticalInterval / 2

    (xLeft, xRight, yLo, yHi)
  }
}

object MandelbrotApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MandelbrotExplorer], args: _*)
  }
}
