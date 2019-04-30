package tsys.stocks

case class Portfolio() {
  // private val stocks = new scala.collection.mutable.HashMap[String, Int]()
  //
  // def add(ticker: String ticker, qty: Int qty) = {
  //   val oldQty = 0
  //   if (stocks(ticker)) {
  //     oldQty = stocks(ticker)
  //   }
  //   stocks += (ticker -> oldQty + qty)
  // }
  //
  // def netWorth(stockService: NationalStockService): Double = {
  //   // ForkJoinPool pool = new ForkJoinPool(10);
  //   val startTime = System.currentTimeMillis()
  //   stocks.keys
  //     // comment or uncomment the parallel() call below and see the difference
  //     .par
  //       .foldLeft(0d) { case(ticker, qty) =>
  //       acc.add(stockService.getPrice(ticker) * entry.getValue());
  //       }
  //         String ticker = entry.getKey();
  //         try {
  //
  //         } catch (Exception e) {
  //           e.printStackTrace();
  //         }
  //       }, ArrayList::addAll);
  //       val worth = prices.stream().reduce(0d, (a, e) -> a + e);
  //       val timeTaken = System.currentTimeMillis() - startTime
  //       println(String.format("Overall Time %d(ms)", timeTaken));
  //       worth
  //   // pool.shutdown();
  //   // return worth.get();
  // }
  
  private val stockService = new NationalStockService

  def track(tickers: String*): List[Double] = {
    tickers.par.flatMap(ticker => {
      val price = stockService.getPrice(ticker).toOption.toList
      println(s"Got $ticker $price using ${Thread.currentThread()}")
      price
    }).toList
    // val prices = new scala.collection.mutable.MutableList[Double]()
    // // tickers.foreach(ticker => stockService.getPrice(ticker).foreach(price => prices += price))
    // tickers.par.foreach(ticker => stockService.getPrice(ticker).foreach(price => {
    //   println(s"Got $ticker $price using ${Thread.currentThread()}")
    //   prices += price
    // }))
    // prices.toList
  }
}
