import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import org.json.JSONObject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;
import io.reactivex.disposables.*;

public class Portfolio {
  private final Map<String, Integer> stocks = new ConcurrentHashMap<>();
  
  public void add(String ticker, Integer qty) {
    int oldQty = 0;
    if (stocks.containsKey(ticker)) {
      oldQty = stocks.get(ticker);
    }
    stocks.put(ticker, oldQty + qty);
  }
	
  public Flowable<Double> netWorth(Flowable<JSONObject> tickers) throws Exception {
    return tickers
      .filter(tick -> stocks.containsKey(tick.getString("ticker")))
      .scan(new HashMap<String, Double>(), (acc, tick) -> {
        String ticker = tick.getString("ticker");
        acc.put(ticker, stocks.get(ticker) * tick.getDouble("price"));
        return acc;  
      })
      .map(worth -> worth.values().stream().reduce(0d, (a, e) -> a + e));
  }
  
  public static void main(String[] args) throws Exception {
    Portfolio portfolio = new Portfolio();
    portfolio.add("GOOG", 10);
    portfolio.add("AAPL", 20);
    portfolio.add("YHOO", 30);
    portfolio.add("MSFT", 40);

    RealTimeNationalStockService stockService = new RealTimeNationalStockService();
    Flowable<JSONObject> realtimePrices = stockService.asFlowable()
      .map(message -> new JSONObject(message))
      .filter(json -> json.has("ticker"))
      .share();
	
    // Create a stream using realtimePrices for showing buy prices with brokerage
    // add 2% brokerage to every price that the user sees.
    double brokerage = 0.02;
    Flowable<JSONObject> realtimePricesWithBrokerage = realtimePrices	  
      .map(message -> {
        double brokeredPrice = message.getDouble("price") * (1 + brokerage);
        message.put("price", brokeredPrice);	
        return message;
      });
     	
    // Create another stream using realtimePrices for showing buy prices with brokerage
    // Calculate Networth of the portfolio on every tick of any stock in it.
    Flowable<Double> netWorth = portfolio.netWorth(realtimePrices);
	
    Disposable pricesWithBrokerage = realtimePricesWithBrokerage
      .subscribe(tick -> System.out.println("Price => " + tick.getDouble("price")), 
        error -> System.out.println("Error => " + error),
        () -> System.out.println("*** DONE Prices With Brokerage ***"));
	
    Disposable worth = netWorth
      .subscribe(total -> System.out.println("NetWorth => " + total), 
        error -> System.out.println("Error => " + error),
        () -> System.out.println("DONE"));
    
    Thread.sleep(6000);
    
    pricesWithBrokerage.dispose();
    
    Thread.sleep(8000);
    
    worth.dispose();
  }
}
