package tsys.stocks

object App {
  def main(args: Array[String]): Unit = {
    val p = Portfolio()
    val startTime = System.currentTimeMillis
    println(p.track("GOOG", "AAPL", "YHOO", "MSFT", "AMZN"))    
    val diff = System.currentTimeMillis - startTime
    println(s"Took $diff (ms)")
  }
}
