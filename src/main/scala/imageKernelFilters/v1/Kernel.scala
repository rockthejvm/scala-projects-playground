package imageKernelFilters.v1

/*
    A kernel class. Has a width, height, and an array of Doubles.

    The `values` array will be the coefficients for convolution that you saw in the explanations.
    It always has width * height elements.
    Think of it as a linearized version of the "matrix" versions you saw in the examples.
*/
case class Kernel(width: Int, height: Int, values: Array[Double]) {

  // a sanity check function - remember this approach as a good practice
  def hasSameSizeAs(other: Kernel): Boolean =
    width == other.width && height == other.height && values.length == other.values.length

  /*
       Multiplies this Kernel with a scalar (a Double) and returns a new Kernel.
       All the values of the resulting Kernel = this Kernel's values, times the scalar.
   */
  def multiply(factor: Double): Kernel =
    new Kernel(width, height, values.map(_ * factor))

  /*
       A handy toString representation.
   */
  override def toString: String = {
    val lines = (0 until height).map(y => values.slice(width * y, width * (y + 1)).mkString(" "))
    s"""size ${values.length}:
       |${lines.mkString("\n")}"
       |""".stripMargin
  }

  def normalize(): Kernel = {
    // YOUR CODE HERE (1-10 lines depending on approach)
    val sum = values.sum // or use a .reduce or .fold
    if (sum == 0) this // can't be normalized
    else new Kernel(width, height, values.map(_ / sum))
  }
}

object Kernel {
  val sharpen3 = new Kernel(3, 3, Array(
        0,-1,0,
        -1,5,-1,
        0,-1,0
      )
    ).normalize()

    val blur3 = new Kernel(3, 3, Array(
        1,2,1,
        2,4,2,
        1,2,1
      )
    ).normalize()

    val outline = new Kernel(3, 3, Array(
        -1,-1,-1,
        -1,8,-1,
        -1,-1,-1
      )
    ).normalize()

    val emboss = new Kernel(3, 3, Array(
        -2,-1,0,
        -1,1,1,
        0,1,2
      )
    ).normalize()

    val unsharp = new Kernel(5,5,Array(
        1, 4, 6, 4, 1,
        4, 16, 24, 16, 4,
        16, 24, -476, 24, 16,
        4, 16, 24, 16, 4,
        1, 4, 6, 4, 1
      )
    ).normalize()

  val blur5 = new Kernel(5,5,
    Array(
      0.003765,0.015019,0.023792,0.015019,0.003765,
      0.015019,0.059912,0.094907,0.059912,0.015019,
      0.023792,0.094907,0.150342,0.094907,0.023792,
      0.015019,0.059912,0.094907,0.059912,0.015019,
      0.003765,0.015019,0.023792,0.015019,0.003765
    )
  ).normalize()

  val sobel1 = new Kernel(3,3,
    Array(
      1, 0, -1,
      2, 0, -2,
      1, 0, -1
    )
  )
  val sobel2 = new Kernel(3,3,
    Array(
      1, 2, 1,
      0, 0, 0,
      -1, -2, -1
    )

  )
}