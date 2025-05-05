package huffmanZipper.algorithm

import huffmanZipper.dataStructures.{HuffmanNode, HuffmanPQ}

import scala.annotation.tailrec

// takes text and creates the first priority queue of huffman nodes (leaves)
object TextAnalyzer {

  // considers sorted text, finds the length of the same-char sequence
  @tailrec
  def firstIndexWithDifferentCharacter(text: String, char: Char, lastCheckedIndex: Int = 0): Int = {
    val newIndex = lastCheckedIndex + 1
    if (text.length > newIndex && char == text.charAt(newIndex)) firstIndexWithDifferentCharacter(text, char, newIndex)
    else newIndex
  }

  def createFirstHuffmanPQ(text: String): HuffmanPQ = {
    def stackVersion(remainingText: String): HuffmanPQ = {
      if (remainingText.isEmpty) HuffmanPQ.empty
      else {
        val firstChar = remainingText.charAt(0)
        val firstLength = firstIndexWithDifferentCharacter(remainingText, firstChar)
        val firstNode = HuffmanNode(Character.toString(firstChar), firstLength)
        stackVersion(remainingText.substring(firstLength)).add(firstNode)
      }
    }

    @tailrec
    def tailVersion(remainingText: String, currentQueue: HuffmanPQ): HuffmanPQ = {
      if (remainingText.isEmpty) currentQueue
      else {
        val firstChar = remainingText.charAt(0)
        val firstLength = firstIndexWithDifferentCharacter(remainingText, firstChar)
        val firstNode = HuffmanNode(Character.toString(firstChar), firstLength)
        tailVersion(remainingText.substring(firstLength), currentQueue.add(firstNode))
      }
    }

    tailVersion(text, HuffmanPQ.empty)
  }

}
