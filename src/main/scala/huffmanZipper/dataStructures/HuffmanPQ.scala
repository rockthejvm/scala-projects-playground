package huffmanZipper.dataStructures

trait HuffmanPQ {
  // inserts this node in sorted order
  def add(node: HuffmanNode): HuffmanPQ

  // the first (lowest-value) element in this priority queue
  def head: HuffmanNode

  // the rest of this PQ
  def tail: HuffmanPQ

  // true if this PQ doesn't have elements
  def isEmpty: Boolean

  // returns the rest of the PQ after extracting an element
  def pop: HuffmanPQ

  // lists all elements in a comma-separated string
  def listElements: String

  // we wrote this for pretty printing, no need to change
  override def toString: String = "[" + listElements + "]"
}

case object EmptyPQ extends HuffmanPQ {
  // inserts this node in sorted order
  def add(node: HuffmanNode): HuffmanPQ = ConsPQ(node, this)

  // the first (lowest-value) element in this priority queue
  def head: HuffmanNode = throw new NoSuchElementException("Empty Huffman queue")

  // the rest of this PQ
  def tail: HuffmanPQ = throw new NoSuchElementException("Empty Huffman queue")

  // true if this PQ doesn't have elements
  def isEmpty: Boolean = true

  // returns the rest of the PQ after extracting an element
  def pop: HuffmanPQ = throw new NoSuchElementException("Empty Huffman queue")

  // lists all elements in a comma-separated string
  def listElements: String = ""

  // we wrote this for pretty printing, no need to change
  override def toString: String = "[" + listElements + "]"
}

object HuffmanPQ {
  def empty: HuffmanPQ = EmptyPQ
  def single(head: HuffmanNode): HuffmanPQ = ConsPQ(head, EmptyPQ)
  def from(head: HuffmanNode, tail: HuffmanPQ) = ConsPQ(head, tail)
}

// pretty much a linked list with insertion sort.
// Note: this is quite inefficient because each add is O(N) - you can implement your own min-heap for O(log(N)) per add
case class ConsPQ(override val head: HuffmanNode, override val tail: HuffmanPQ) extends HuffmanPQ {
  // true if this PQ doesn't have elements
  def isEmpty: Boolean = false

  // inserts this node in sorted order
  def add(node: HuffmanNode): HuffmanPQ =
    if (node < head) ConsPQ(node, this)
    else ConsPQ(head, tail.add(node))

  // returns the rest of the PQ after extracting an element
  def pop: HuffmanPQ = tail

  // lists all elements in a comma-separated string
  def listElements: String =
    if (tail.isEmpty) s"$head"
    else s"$head,${tail.listElements}"

  // we wrote this for pretty printing, no need to change
  override def toString: String = "[" + listElements + "]"
}

object ConsPQTest {
  def main(args: Array[String]): Unit = {
    val empty = EmptyPQ
    val oneElement = empty.add(HuffmanNode("A", 2))
    val anotherElement = oneElement.add(HuffmanNode("B", 4))
    val elementInBetween = anotherElement.add(HuffmanNode("C", 3))
    println(elementInBetween)
  }
}