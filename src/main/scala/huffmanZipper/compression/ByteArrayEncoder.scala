package huffmanZipper.compression

import huffmanZipper.algorithm.{QueueAnalyzer, TextAnalyzer}
import huffmanZipper.dataStructures.{HuffmanNode, HuffmanPQ}

import scala.annotation.tailrec

object ByteArrayEncoder extends Encoder[Array[Byte]] {

  def encodeNodes(nodes: HuffmanPQ): Array[Byte] = {
    def encodeTailrec(remainingNodes: HuffmanPQ, currentEncoding: Array[Byte] = Array()): Array[Byte] =
      if (remainingNodes.isEmpty) currentEncoding
      else {
        val currentNode = remainingNodes.head
        // I know the primal nodes are single-char
        val charByte: Byte = currentNode.chars.charAt(0).toByte
        val occByte1: Byte = (currentNode.occurrences & 0xFF).toByte
        val occByte2: Byte = ((currentNode.occurrences & 0xFF00) >> 8).toByte
        val occByte3: Byte = ((currentNode.occurrences & 0xFF0000) >> 16).toByte
        val occByte4: Byte = ((currentNode.occurrences & 0xFF000000) >> 24).toByte
        val nodeBytes: Array[Byte] = Array(charByte, occByte1, occByte2, occByte3, occByte4)
        encodeTailrec(remainingNodes.tail, currentEncoding ++ nodeBytes)
      }

    val bytes = encodeTailrec(nodes)
    val nBytes = bytes.length
    val nBytes1 = (nBytes & 0xFF).toByte
    val nBytes2 = ((nBytes & 0xFF00) >> 8).toByte
    val nBytes3 = ((nBytes & 0xFF0000) >> 16).toByte
    val nBytes4 = ((nBytes & 0xFF000000) >> 24).toByte
    println(s"Writing $nBytes")
    println(s"$nBytes1, $nBytes2, $nBytes3, $nBytes4")
    Array(nBytes1, nBytes2, nBytes3, nBytes4) ++ bytes
  }

  override def encode(text: String): Array[Byte] = {
    @tailrec
    def compressTailrec(remainingText: String, huffmanTree: HuffmanNode, currentChar: Char, currentNode: HuffmanNode, currentEncoding: BigInt = 1): BigInt = {
      if (remainingText.isEmpty && currentNode.isLeaf) currentEncoding
      else if (currentNode.isLeaf) {
        compressTailrec(remainingText.substring(1), huffmanTree, remainingText.charAt(0), huffmanTree, currentEncoding)
      } else {
        if (currentNode.left.chars.contains(currentChar)) compressTailrec(remainingText, huffmanTree, currentChar, currentNode.left, currentEncoding << 1)
        else compressTailrec(remainingText, huffmanTree, currentChar, currentNode.right, (currentEncoding << 1) | 1)
      }
    }

    val huffmanQueue = TextAnalyzer.createFirstHuffmanPQ(text.sorted)
    val huffmanTree = QueueAnalyzer.getFinalHuffmanNode(huffmanQueue)
    val encodedContent =
      if (text.isEmpty) Array[Byte]()
      else compressTailrec(text.substring(1), huffmanTree, text.charAt(0), huffmanTree).toByteArray

    val encodedTree = encodeNodes(huffmanQueue)
    encodedTree ++ encodedContent
  }
}
