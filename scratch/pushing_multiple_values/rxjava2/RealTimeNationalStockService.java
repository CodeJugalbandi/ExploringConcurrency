import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.disposables.*;

public class RealTimeNationalStockService {

  private final String uriTemplate = "wss://national-stock-service.herokuapp.com/stocks/realtime/%s";
  // private final String uriTemplate = "ws://localhost:5000/stocks/realtime/%s";

  private Map<String, SubscriberSocket> subscribers = new HashMap<>();

  public String subscribeTo(String ticker, Consumer<String> onMessage, Consumer<Throwable> onError,
      BiConsumer<Integer, String> onClose) throws MalformedURLException, URISyntaxException {
		  
    final String wssUri = (ticker == null || ticker.isEmpty()) ? String.format(uriTemplate, "") : String.format(uriTemplate, ticker);
    SubscriberSocket subscriberSocket = new SubscriberSocket(new URI(wssUri), onMessage, onError, onClose);
    subscriberSocket.connect();
    System.out.println("Connecting to RealTimeNationalStockService..." + wssUri);
    String uuid = UUID.randomUUID().toString();
    subscribers.put(uuid, subscriberSocket);
    while (subscriberSocket.isConnecting());
    return uuid;
  }

  public void unsubscribe(String clientId) {
    if (subscribers.containsKey(clientId)) {
      final SubscriberSocket subscriberSocket = subscribers.get(clientId);
      if (subscriberSocket.isOpen())
        subscriberSocket.send("{ \"command\" : \"unsubscribe\" }");
    }
  }

  public void close(String clientId) {
    if (subscribers.containsKey(clientId)) {
      final SubscriberSocket subscriberSocket = subscribers.get(clientId);
      if (subscriberSocket.isOpen()) {
        subscriberSocket.close();
        subscribers.remove(clientId);
      }
    }
  }
  
  public Flowable<JSONObject> asFlowable() {
    return asFlowable("");
  }
	
  public Flowable<JSONObject> asFlowable(String ticker) {
    String statusMsg = String.format("RealTimeNationalStockServiceObservable.asFlowable(%s): ", ticker);
    System.out.println(statusMsg + "Ready...");
    return Flowable.<String>create(subscriber -> {
      try {
        final String clientId = subscribeTo(ticker, 
          message -> subscriber.onNext(message), 
          error -> subscriber.onError(error), 
          (code, reason) -> subscriber.onComplete());

    	subscriber.setCancellable(() -> {
          if (!clientId.isEmpty()) {
            System.out.println(statusMsg + "Unsubscribing..."); 
            unsubscribe(clientId);
            System.out.println(statusMsg + "Closing...");             
            close(clientId);
            System.out.println(statusMsg + "Closed.");                         
          }
    	});
      } catch (MalformedURLException | URISyntaxException e) {
        subscriber.onError(e);
      } 
    }, BackpressureStrategy.DROP)
    .subscribeOn(Schedulers.io())
    .map(message -> new JSONObject(message));
  }

  private static class SubscriberSocket extends WebSocketClient {
    private Consumer<String> onMessage;
    private Consumer<Throwable> onError;
    private BiConsumer<Integer, String> onClose;

    public SubscriberSocket(URI serverUri, Consumer<String> onMessage, Consumer<Throwable> onError,
        BiConsumer<Integer, String> onClose) {
      super(serverUri);
      this.onMessage = onMessage;
      this.onError = onError;
      this.onClose = onClose;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
      System.out.println("Connected");
	  send("{ \"command\" : \"subscribe\" }");
    }

    @Override
    public void onMessage(String message) {
      onMessage.accept(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
      onClose.accept(code, reason);
      System.out.println("Disconnected");
    }

    @Override
    public void onError(Exception ex) {
      onError.accept(ex);
    }
  }

  public static void main(String[] args) throws Exception {
    RealTimeNationalStockService stockService = new RealTimeNationalStockService();
    Disposable disposable = stockService.asFlowable()
      .doOnNext(System.out::println)
      .filter(json -> json.has("ticker"))
      .map(tick -> Arrays.asList(tick.getString("ticker"), tick.getDouble("price")))
      .subscribe(tick -> System.out.println("Ticker Price => " + tick), 
        error -> System.out.println("Error => " + error),
        () -> System.out.println("DONE"));
    
    System.out.println("Will stop receiving prices after 15 seconds...");
    Thread.sleep(TimeUnit.SECONDS.toMillis(15));
    System.out.println("Disposing the subscriber...");
    disposable.dispose();
    System.out.println("Disposed the Subscriber!");
    System.out.println("DONE");
  }
}

