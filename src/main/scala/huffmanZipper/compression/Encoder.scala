package huffmanZipper.compression

import huffmanZipper.dataStructures.HuffmanNode

// should be covariant but I'm not going to mention this in the assignment if I use it
trait Encoder[T] {
  // takes the original string to encode and returns its "compressed" encoding as T.
  def encode(text: String): T
}
