package playground

object Playground{
  abstract class VetC[-T] {
    // S is sub type of T
    def rescueAnimal[S <: T](animal: S): S = animal // assuming your animal is healed
  }


  def f(n: => Int) = n + 1
  def g(n: () => Int) = n() + 1

  class Person(private val n: String, private var a: Int) {
    def age_=(newage: Int): Unit = a = newage
    def age = a
  }

  def main(args: Array[String]): Unit = {
    val alice = new Person("Alice", 24) // don't confuse visibility here with the fact that we can actually pass arguments
    // val aliceName = alice.name // not working
    alice.age = 25
  }
}
