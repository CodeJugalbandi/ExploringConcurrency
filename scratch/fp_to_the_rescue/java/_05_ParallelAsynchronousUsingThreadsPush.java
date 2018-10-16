import java.util.*;
import java.net.*;
import java.io.*;
import java.util.function.*;
import java.util.concurrent.*;

class Request {
  private final	URL url;
  private BiConsumer<String, Throwable> handler;
  
  Request(URL url) {
    this.url = url;
  }
  
  public void onComplete(BiConsumer<String, Throwable> handler) {
    this.handler = handler;
  }
  
  public void get() {
    if (handler == null)
      throw new RuntimeException("Handler Not Registered, use onComplete()");
	
    new Thread(() -> {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line = null;
        StringBuilder data = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          data.append(line); 
        }
        handler.accept(data.toString().trim(), null);
      } catch(Throwable t) {
        handler.accept(null, t);
      }
    }).start();
  }
}

class Result<T> {
  public T data;
  public Throwable error;	
}

class _05_ParallelAsynchronousUsingThreadsPush {
  public static void main(String[] args) throws Exception {
    // URL placesNearbyUrl = new URL ("http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km");
    URL placesNearbyUrl = new URL("https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km");
    // URL weatherUrl = new URL("http://localhost:8000/weather?lat=19.01&lon=72.8");
    URL weatherUrl = new URL("https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8");
    final Request weatherRequest = new Request(weatherUrl);
    final Request placesNearbyRequest = new Request(placesNearbyUrl);
    final CountDownLatch latch = new CountDownLatch(2);
    final Result<String> placesNearby = new Result<>();
    placesNearbyRequest.onComplete((data, error) -> {
      placesNearby.data = data;
      placesNearby.error = error;
      latch.countDown();
    });
    final Result<String> weather = new Result<>();
    weatherRequest.onComplete((data, error) -> {
      weather.data = data;
      weather.error = error;
      latch.countDown();
    });
	
    long startTime = System.currentTimeMillis();	  
    weatherRequest.get();
    placesNearbyRequest.get();
    latch.await(); // explicit synchronization point
    long timeTaken = System.currentTimeMillis() - startTime;
	
    if (placesNearby.error != null)
      throw new RuntimeException(placesNearby.error);
    if (weather.error != null)
      throw new RuntimeException(weather.error);
	
    String weatherAndPlacesNearby = String.format("{ \"weather\" : %s, \"placesNearby\": %s }", weather.data, placesNearby.data);
    System.out.println(String.format("Time Taken %d(ms)", timeTaken));
    System.out.println(weatherAndPlacesNearby);
  }
}