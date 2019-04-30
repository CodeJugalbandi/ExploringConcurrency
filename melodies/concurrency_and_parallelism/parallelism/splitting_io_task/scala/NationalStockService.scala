package tsys.stocks

import scala.util.{Try, Success, Failure}

class NationalStockService {
  private val urlTemplate = "https://national-stock-service.herokuapp.com/stocks/"
  
  def getPrice(ticker: String): Try[Double] = {
    val url = s"$urlTemplate$ticker"
    println(s"Fetching $ticker price using ${Thread.currentThread()}")
    Try {
      io.Source.fromURL(url).mkString
    }.flatMap(data => {
      import scala.util.parsing.json._
      val json: Option[Any] = JSON.parseFull(data)
      val price: Option[Double] = json.map(response => {
        val values = response.asInstanceOf[Map[String, Any]]
        values("price").asInstanceOf[Double]
      })
      Try { price.get }
    })
  }
}