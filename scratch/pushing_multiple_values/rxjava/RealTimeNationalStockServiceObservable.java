import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import org.json.JSONObject;
import rx.Observable;
import rx.schedulers.Schedulers;

public class RealTimeNationalStockServiceObservable {
  private static RealTimeNationalStockService stockService = new RealTimeNationalStockService();

  public static Observable<String> forAllStocks() {
    return forStock("");
  }
	
  public static Observable<String> forStock(String ticker) {
    System.out.println("RealTimeNationalStockServiceObservable.forStock() : Ready...");
    return Observable.<String>unsafeCreate(subscriber -> {
      String clientId = "";
      try {
        clientId = stockService.subscribeTo(ticker, 
          message -> subscriber.onNext(message), 
          error -> subscriber.onError(error), 
          (code, reason) -> subscriber.onCompleted());
          while (!subscriber.isUnsubscribed());
          subscriber.onCompleted();
      } catch (MalformedURLException | URISyntaxException e) {
        subscriber.onError(e);
      } finally {
        if (!clientId.isEmpty()) {
          stockService.unsubscribe(clientId);
          stockService.close(clientId);
          System.out.println("RealTimeNationalStockServiceObservable : Closed Subscription");
        }
      }
    })
    .subscribeOn(Schedulers.io());
  }

  private static void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) throws URISyntaxException, IOException {
    System.out.println(">> Press any key to terminate.... <<");
    Observable<List<?>> prices = RealTimeNationalStockServiceObservable.forAllStocks()
    // Observable<Double> prices = RealTimeNationalStockServiceObservable.forStock("AMZN")
	  .doOnNext(System.out::println)
      .map(message -> new JSONObject(message))
      .filter(json -> json.has("ticker"))
      .map(tick -> Arrays.asList(tick.getString("ticker"), tick.getDouble("price")));
			 

		prices.onBackpressureDrop(droppedItem -> System.out.println("DROPPED ===> " + droppedItem))
				.observeOn(Schedulers.io())
				// .take(10)
				.subscribe(price -> {
					System.out.println("Next => " + price + " on " + Thread.currentThread());
					// sleep(10 * 1000);
					System.out.println("Processed => " + price + " on " + Thread.currentThread());
				}, error -> System.out.println("Error => " + error + " on " + Thread.currentThread()),
						() -> System.out.println("DONE on " + Thread.currentThread()));
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
		}
		System.in.read();
	}
}
