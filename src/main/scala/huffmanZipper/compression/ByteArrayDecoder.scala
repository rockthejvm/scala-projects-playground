package huffmanZipper.compression

import huffmanZipper.algorithm.QueueAnalyzer
import huffmanZipper.dataStructures.{HuffmanNode, HuffmanPQ}

object ByteArrayDecoder extends Decoder[Array[Byte]] {

  // needed because byte.toInt keeps the sign of the byte, which makes things awkward
  def fourBytesToInt(byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte) = {
    def byteToUnsigned(b: Byte): Int =
      if (b >= 0) b.toInt
      else 256 + b

    byteToUnsigned(byte1) | (byteToUnsigned(byte2) << 8) | (byteToUnsigned(byte3) << 16) | (byteToUnsigned(byte4) << 24)
  }

  def decodeArray(bytes: Array[Byte]): String = {
    // Reconstructs the Huffman priority queue.
    // Queue =
    //    - first 4 bytes = an int, representing the number of bytes in the PQ (nBytes)
    //    - nBytes/5 groups of 5 bytes each. Each group is (character = 1 byte)(nOccurrences = 4 bytes)
    // The queue is given in order, but the add method of the HuffmanPQ already takes care of sorted insertion.
    def reconstructQueue(bytes: Array[Byte]): HuffmanPQ = {
      def reconstructTailrec(bytesLeft: Array[Byte], currentQueue: HuffmanPQ): HuffmanPQ =
        if (bytesLeft.isEmpty) currentQueue
        else {
          val char = bytesLeft(0).toChar
          val occurrences = fourBytesToInt(bytesLeft(1), bytesLeft(2), bytesLeft(3), bytesLeft(4))
          val node = HuffmanNode(char.toString, occurrences)
          reconstructTailrec(bytesLeft.drop(5), currentQueue.add(node))
        }

      reconstructTailrec(bytes, HuffmanPQ.empty)
    }

    val nBytesQueue = fourBytesToInt(bytes(0), bytes(1), bytes(2), bytes(3))
    val queueBytes = bytes.slice(4, nBytesQueue + 4)
    val queue = reconstructQueue(queueBytes)
    val contentBytes = bytes.drop(nBytesQueue + 4)
    val huffmanTree = QueueAnalyzer.getFinalHuffmanNode(queue)

    decode(contentBytes, huffmanTree)
  }

  override def decode(data: Array[Byte], huffmanTree: HuffmanNode): String = {
    // the binary representation of the data - easier to traverse bit by bit than an array of bytes
    val bigint = BigInt(data)

    // traverses the tree by the bits of the bigInt and reconstructs the string
    // reached a leaf? new character
    // 0=move left, 1=move right.
    def decodeTailrec(index: Int, currentNode: HuffmanNode, currentString: String): String = {
      if (index < 0) currentString + currentNode.chars
      else if (currentNode.isLeaf) decodeTailrec(index, huffmanTree, currentString + currentNode.chars)
      else if (bigint.testBit(index)) decodeTailrec(index - 1, currentNode.right, currentString)
      else decodeTailrec(index - 1, currentNode.left, currentString)
    }

    decodeTailrec(bigint.bitLength - 2, huffmanTree, "")
  }
}
