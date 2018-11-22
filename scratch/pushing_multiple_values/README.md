# Streaming Events (Pushing Multiple Values)

In this melody, we will look at how reactive programming uses Functional Programming and helps with the concurrency in the system.  

## Problem Statement

We will demonstrate this using problem statement - Given a bunch of stocks in user's portfolio, when the user subscribes to receiving price updates for them in real-time, then the porfolio calculates its Networth on every tick of any stock in it.

Finally, user sees buy prices for all the stocks.  A 2% brokerage is added to every price that the user sees when purchasing.

## CodeJugalbandi

**BRAHMA** In Clojure, we have the core-async library that can help us create a highly concurrent system. The core abstraction here is a Channel.  Imagine a channel like a pipe, where at one end can push messages and listen to them at the other end by registering a listener.

```clojure
defn create-message-stream [url & {:keys [on-connect before-disconnect]
                                    :or {on-connect identity before-disconnect identity}}]
  (let [ws-conn     (http/websocket-client url)
        ch          (chan (dropping-buffer 32) (map parse-message))
        stop?       (atom false)
        stop-fn     (fn []
                      (reset! stop? true)
                      (before-disconnect ws-conn)
                      (s/close! @ws-conn)
                      (async/close! ch))]

    (go-loop []
      (when-let [message @(s/take! @ws-conn)]
        (>! ch message)
        (if-not @stop?
          (recur))))

    (on-connect ws-conn)
    {:ch ch :stop-fn stop-fn}))
```

**BRAHMA** A channel is set-up by using the ```chan``` function.  To this function, we pass a ```dropping-buffer``` which essentially drops the messages that it receives when the internal buffer becomes full.  We then transform the string messages it receives from the Websocket connection ```ws-conn``` to JSON and further to a Clojure Map using the transformation function ```keyword``` 

```clojure
(defn- parse-message [message-str] (json/parse-string message-str keyword))
```

**BRAHMA** The channel is set into a continuous ```go-loop``` and ```recur```, it loops until the stop function is called.  We read the message from ```ws-conn``` by ```deref```ing it and the associated stream.  This message is pushed on to the channel and is now available for the listeners.


![Clojure-Channels](Clojure-Channels.png)

// Jaju, please fill up clojure here

**KRISHNA** Let me show you this using Reactive Extensions in Java.

```java
// Runner.java
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
```
![Rx-Observables](Rx-Observables.png)

**KRISHNA** As, you can see that ```nationalStockExchangeFeed()``` returns a continuous real-time price from the National Stock Service as a ```Flowable<JSONObject>```.  In RxJava2 there are two types, ```Observable``` and ```Flowable```.  The difference between them is that ```Flowable``` can handle backpressure, while Observable cannot.  In RxJava1, it was ```Observable``` only.  For the purposes of the current   explanation, I'll ignore this technical difference and treat ```Flowable``` and ``Observable`` in an informal sense by treating them synonymously.  So, though the code says ```Flowable```, I'll refer to it as an ```Observable```.  I'll come to ```Flowable``` when we see that piece of code.

```java
// Runner.java
static Flowable<JSONObject> nationalStockExchangeFeed() {
  return new RealTimeNationalStockService()
    .asFlowable()
    .filter(json -> json.has("ticker"));
}
```


**KRISHNA** This function uses ```RealTimeNationalStockService``` - a stand-in that subscribes to the realtime prices from the National Stock Service using Web-Socket and has an ```asFlowable()``` method to create an Observable.  As we receive other JSON messages along with stock price message, a filter has been set-up to allow only pricing related messages.  Now, lets look at the ```asFlowable()``` method of the ```RealTimeNationalStockService```

```java
// RealTimeNationalStockService.java
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
  .observeOn(Schedulers.computation())
  .map(message -> new JSONObject(message))
  .share();
}
```

**KRISHNA** It creates a Flowable<JSONObject> using the ```create()``` factory method.  This is where we ```subscribeTo()``` to the callbacks provided by this service.  Additionally, we set the cancellation callback using the ```setCancellable()``` which gets invoked when there are no subscribers left or there is an explicit call to ```dispose()``` the subscription.  Afterall, we don't want to hang-on an expensive Web-Socket connection and go on pushing messages when no one is subscribed.  


Also, using ```share()```, we make this Observable hot.  In ```Rx```, an observable can either be hot or cold.  Hot observables are shared by all the subscribers, whereas when a subscriber connects to a cold observable, it gets a brand new observable with an new Web-Socket connection.  So, to prevent this expensive resource creation again and again, we do resource sharing using the ```share()``` operator.

**KRISHNA**  I have created  ```Flowable```

## Reflections

**BRAHMA** 


**KRISHNA** 


