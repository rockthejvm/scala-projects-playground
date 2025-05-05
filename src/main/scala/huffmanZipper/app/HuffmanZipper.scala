package huffmanZipper.app

import huffmanZipper.algorithm.{QueueAnalyzer, TextAnalyzer}
import huffmanZipper.compression.{BinaryStringDecoder, BinaryStringEncoder, ByteArrayDecoder, ByteArrayEncoder}

object HuffmanZipper {
  def testStringEncoding() = {
    // TEST
    val text = "BACADAEAFABBAAAGH"

    val huffmanQueue = TextAnalyzer.createFirstHuffmanPQ(text.sorted)
    val huffmanTree = QueueAnalyzer.getFinalHuffmanNode(huffmanQueue)

    val encodedText = BinaryStringEncoder.encode(text)
    println(encodedText)
    println(encodedText.length)
    val decodedText = BinaryStringDecoder.decode(encodedText, huffmanTree)
    println(decodedText)
    println(text == decodedText)
  }

  def testByteEncoding() = {
    val test = "BACADAEAFABBAAAGH"
    val loremIpsum =
      """
        |Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam iaculis leo a sapien faucibus vulputate. Pellentesque vitae dui vel eros rutrum blandit. Suspendisse auctor metus non nisi aliquet, et convallis libero vulputate. Nullam maximus eu erat eget mattis. Sed scelerisque et ex sed dignissim. Aliquam hendrerit consectetur turpis a condimentum. Vivamus justo dolor, consequat vel iaculis a, pulvinar sit amet purus. Nulla viverra tincidunt ex, non porttitor dolor tincidunt vitae.
        |
        |Suspendisse vel tellus nec massa faucibus tempor at id nisl. Nulla sed est a enim euismod ullamcorper. Cras eget felis vitae est convallis gravida sit amet eu neque. Sed condimentum bibendum tempus. Nulla sodales maximus erat quis porta. Fusce sagittis nisi et porttitor molestie. Integer congue lacus in mauris porttitor cursus. Fusce justo magna, ultricies et mauris placerat, fringilla dignissim lorem. Nulla pharetra sapien sed mi placerat, eget tristique sapien interdum. Maecenas et felis in lacus cursus maximus. Aliquam facilisis, magna eu varius feugiat, ipsum urna fermentum lectus, sed consectetur elit augue sit amet dolor. Nunc porta justo ac auctor porta. Duis pulvinar varius dolor, et varius neque tristique nec. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vivamus imperdiet et velit sit amet condimentum. Quisque iaculis libero lacus, vitae fermentum felis ornare sit amet.
        |
        |Proin placerat sit amet dui at luctus. Vivamus malesuada justo id orci ultricies dictum. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Mauris pretium lorem enim, nec consequat turpis lobortis vel. Pellentesque convallis, mauris at dictum placerat, neque tellus euismod nisi, at vehicula odio est a nisi. Maecenas non maximus tellus. Ut ultrices quam eget nisi sagittis egestas. Nunc quam erat, efficitur sed gravida porta, ornare id quam. Integer volutpat porttitor lorem. Donec sed leo ex.
        |
        |Quisque neque ligula, lacinia eu condimentum ac, porttitor feugiat velit. Phasellus at magna ornare ligula vehicula ultrices a quis tellus. Integer ullamcorper vitae velit ut sodales. Nullam et congue purus. Fusce varius velit pellentesque, porttitor diam quis, posuere massa. Curabitur in mattis dolor, nec eleifend leo. In tincidunt nibh quis sem laoreet accumsan. Aliquam laoreet posuere lectus, a sollicitudin dolor. Praesent non volutpat sem, in vehicula felis. Mauris vel ipsum quis nisl semper pharetra. Duis fringilla mauris in velit dictum gravida.
        |
        |Integer vestibulum lectus eget posuere cursus. Phasellus non varius est. Aenean vulputate tristique lacus, sed pellentesque augue porttitor ac. Suspendisse et justo in augue pulvinar imperdiet. Vestibulum suscipit consequat urna sed lobortis. Morbi consectetur dui et ex elementum interdum. Sed consectetur congue elit condimentum tempus. Duis vehicula nibh posuere, mattis purus eget, eleifend quam. Aenean sit amet mi at nibh sagittis molestie eu aliquet purus. Nunc eget est in erat gravida hendrerit eget id mi. Maecenas ut facilisis nunc, id lacinia libero. Suspendisse potenti. Praesent orci neque, tincidunt eu ipsum at, laoreet sagittis nisl.
        |
        |In venenatis eu nunc at tempus. Quisque pellentesque eget neque ut posuere. Duis rutrum leo ut porta tincidunt. Duis interdum lobortis elit. Fusce sed quam placerat eros lobortis hendrerit a ut nisl. Morbi ipsum orci, tincidunt a luctus.
        |""".stripMargin
    val encodedText = ByteArrayEncoder.encode(test)
    val decodedText = ByteArrayDecoder.decodeArray(encodedText)
    println(decodedText)
    println(test == decodedText)
  }

  def main(args: Array[String]): Unit = {
    testByteEncoding()
  }
}
