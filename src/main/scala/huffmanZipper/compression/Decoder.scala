package huffmanZipper.compression

import huffmanZipper.dataStructures.HuffmanNode

// should be contravariant but I'm not going to mention this in the assignment if I use it
trait Decoder[T] {
  // Takes the data and the huffman mapping (the tree) and returns the original string.
  // This method should be just decode(data) but I kept it for the binary string encoding
  //    because encoding the tree inside the string would have been shitty to parse after.
  // Probably best to change the signature to just decode(data: T) when you move to the binary implementation.
  def decode(data: T, huffmanTree: HuffmanNode): String
}
