package huffmanZipper.algorithm

import huffmanZipper.dataStructures.{HuffmanNode, HuffmanPQ}

import scala.annotation.tailrec

// this compresses a priority queue of huffman node leaves into a single huffman tree
object QueueAnalyzer {
  @tailrec
  def getFinalHuffmanNode(queue: HuffmanPQ): HuffmanNode = {
    if (queue.isEmpty) throw new NoSuchElementException("Empty priority queue for tree extraction")
    else if (queue.tail.isEmpty) queue.head
    else {
      val node1 = queue.head
      val queueMinusOne = queue.pop
      val node2 = queueMinusOne.head
      val remainingQueue = queueMinusOne.pop
      val combinedNode = node1 + node2
      getFinalHuffmanNode(remainingQueue.add(combinedNode))
    }
  }

}
