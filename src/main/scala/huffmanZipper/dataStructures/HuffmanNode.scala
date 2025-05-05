package huffmanZipper.dataStructures

// Note: nulls are quite discouraged in FP, but this is an assignment in OOP so it's excused.
case class HuffmanNode(chars: String, occurrences: Int, left: HuffmanNode = null, right: HuffmanNode = null) {
  override def toString: String = "{" + chars + "|" + occurrences + "}"

  def <(other: HuffmanNode) = this.occurrences < other.occurrences

  def +(other: HuffmanNode) = HuffmanNode(this.chars + other.chars, this.occurrences + other.occurrences, this, other)

  def isLeaf = left == null && right == null
}

object HuffmanNode {
  implicit val ordering: Ordering[HuffmanNode] = Ordering.fromLessThan(_ < _)
}
