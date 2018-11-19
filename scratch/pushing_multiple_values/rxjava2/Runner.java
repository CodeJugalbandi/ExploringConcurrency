import java.util.concurrent.*;
import org.json.JSONObject;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;
import io.reactivex.disposables.*;

class Runner {
  static Flowable<JSONObject> nationalStockExchangeFeed() {
    RealTimeNationalStockService stockService = new RealTimeNationalStockService();
    return stockService.asFlowable()
      .filter(json -> json.has("ticker"))
      .share();
  }
  
  static Flowable<JSONObject> pricesWithBrokerage(Flowable<JSONObject> prices, double brokerage) {
    return prices.map(message -> {
      double brokeredPrice = message.getDouble("price") * (1 + brokerage);
      message.put("price", brokeredPrice);	
      return message;
    });
  }
  
  static void stop(Disposable disposable, long time, TimeUnit timeUnit) throws InterruptedException {
    waitFor(time, timeUnit);
    disposable.dispose();
  }
  
  static void waitFor(long time, TimeUnit timeUnit) throws InterruptedException {
    timeUnit.sleep(time);
  }
  
  public static void main(String[] args) throws Exception {
    Flowable<JSONObject> stockPrices = nationalStockExchangeFeed();
	
    // Create a stream using stockPrices for showing buy prices with brokerage
    double brokerage = 0.02;  // add 2% brokerage to every price that the user sees.
    Disposable buyPrices = pricesWithBrokerage(stockPrices, brokerage)
      .subscribe(tick -> System.out.println(String.format("Buy Price => [%s, %f]", tick.getString("ticker"), tick.getDouble("price"))), 
         error -> System.out.println("Error => " + error),
         () -> System.out.println("*** DONE Buy Price ***"));
    
    waitFor(8, TimeUnit.SECONDS);

    Portfolio myPortfolio = new Portfolio();
    myPortfolio.add("GOOG", 40);
    myPortfolio.add("AAPL", 30);
    myPortfolio.add("ORCL", 10);
    myPortfolio.add("MSFT", 10);
    
    Disposable netWorth = myPortfolio.netWorth(stockPrices)
      .subscribe(total -> System.out.println("NetWorth => " + total), 
         error -> System.out.println("Error => " + error),
         () -> System.out.println("*** DONE NetWorth ***"));
    
    stop(buyPrices, 6, TimeUnit.SECONDS);
    System.out.println("Stopped buyPrices");
    stop(netWorth, 6, TimeUnit.SECONDS);
    System.out.println("Stopped NetWorth");
  }
}