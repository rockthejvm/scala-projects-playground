package huffmanZipper.compression

import huffmanZipper.algorithm.{QueueAnalyzer, TextAnalyzer}
import huffmanZipper.dataStructures.HuffmanNode

import scala.annotation.tailrec

object BinaryStringEncoder extends Encoder[String] {
  def encode(text: String): String = {
    // traverses the tree for the final encoding of a char
    // 0 = move to left child, 1 = move to right child
    @tailrec
    def findStringEncodingForChar(char: Char, tree: HuffmanNode, currentEncoding: String = ""): String = {
      if (tree.chars.length == 1 && tree.chars.charAt(0) == char) currentEncoding
      else if (tree.left.chars.contains(char)) findStringEncodingForChar(char, tree.left, currentEncoding + "0")
      else findStringEncodingForChar(char, tree.right, currentEncoding + "1")
    }

    // Takes each character and walks it through the tree for the encoding.
    // Then that encoding is attached to the end result (currentEncoding)
    @tailrec
    def compressTailrec(remainingText: String, huffmanTree: HuffmanNode, currentEncoding: String = ""): String =
      if (remainingText.isEmpty) currentEncoding
      else  {
        val firstCharEncoding = currentEncoding + findStringEncodingForChar(remainingText.charAt(0), huffmanTree)
        compressTailrec(remainingText.tail, huffmanTree, firstCharEncoding)
      }

    val huffmanQueue = TextAnalyzer.createFirstHuffmanPQ(text.sorted)
    println(huffmanQueue)
    val huffmanTree = QueueAnalyzer.getFinalHuffmanNode(huffmanQueue)
    println(huffmanTree)
    compressTailrec(text, huffmanTree)
  }


}
