package scraping

/*
  
 */
object HttpApis {
  def main(args: Array[String]): Unit = {
    // use like Python
    val firstResp = requests.get("https://api.github.com/users/lihaoyi")
    println(firstResp.statusCode)
    println(firstResp.headers("content-type"))
    println(firstResp.text())

    // all http methods
    val r1 = requests.post("http://httpbin.org/post", params = Map("key" -> "value")) // query params
    val r2 = requests.put("http://httpbin.org/put", data = Map("key" -> "value")) // payload data
    val r3 = requests.delete("http://httpbin.org/delete")
    val r4 = requests.options("http://httpbin.org/get")

    val json = ujson.read(r1.text())
    println(json.obj.keySet)
    
    // you can pass headers, use multipart uploads, and get streaming data
    // the library is dead simple
  }
}
