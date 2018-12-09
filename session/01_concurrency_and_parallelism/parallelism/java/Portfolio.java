import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

public class Portfolio {
  private Map<String, Integer> stocks = new HashMap<>();
  
  public void add(String ticker, Integer qty) {
    int oldQty = 0;
    if (stocks.containsKey(ticker)) {
      oldQty = stocks.get(ticker);
    }
    stocks.put(ticker, oldQty + qty);
  }
	
  public Double netWorth(NationalStockService stockService) throws Exception {
    System.out.println("Stocks = " + stocks);
    List<Double> prices = stocks.entrySet()
			.stream()
			.collect(ArrayList<Double>::new, (acc, entry) -> {
        String ticker = entry.getKey();
        try {
          acc.add(stockService.getPrice(ticker) * entry.getValue());  
        } catch (Exception e) {
          e.printStackTrace();
        }
      }, ArrayList::addAll);
    double worth = prices.stream().reduce(0d, (a, e) -> a + e);
    return worth;
  }
  
  public static void main(String[] args) throws Exception {
    Thread.sleep(10000);
    Portfolio portfolio = new Portfolio();
    portfolio.add("GOOG", 10);
    portfolio.add("AAPL", 20);
    portfolio.add("YHOO", 30);
    portfolio.add("MSFT", 40);
    System.out.println("NetWorth = " + portfolio.netWorth(new NationalStockService()));
    System.out.println("DONE");
  }
}
