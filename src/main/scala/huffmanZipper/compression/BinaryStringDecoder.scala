package huffmanZipper.compression

import huffmanZipper.dataStructures.HuffmanNode

import scala.annotation.tailrec

object BinaryStringDecoder extends Decoder[String] {
  override def decode(data: String, huffmanTree: HuffmanNode) = {
    // Given the Huffman tree, it's quite easy to restore the decoded text.
    // Walks through the Huffman tree given by the "bits" in the data.
    // If we reach a leaf node, we reached a character, and we add it to the decoded text.
    // Otherwise, traverse the tree (0=left, 1=right) given by the "bits".
    @tailrec
    def decodeTailrec(currentData: String, node: HuffmanNode, currentText: String): String = {
      if (currentData.isEmpty) currentText + node.chars // reached the end of the tree
      else if (node.isLeaf) decodeTailrec(currentData, huffmanTree, currentText + node.chars)
      else if (currentData.charAt(0) == '0') decodeTailrec(currentData.substring(1), node.left, currentText)
      else decodeTailrec(currentData.substring(1), node.right, currentText)
    }

    if (data.isEmpty) ""
    else decodeTailrec(data, huffmanTree, "")
  }
}
