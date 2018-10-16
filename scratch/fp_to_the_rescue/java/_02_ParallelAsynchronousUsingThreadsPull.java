import java.util.*;
import java.net.*;
import java.io.*;

class Request implements Runnable {
  private String data;
  private Throwable exception;
  private final URL url;

  Request(URL url) {
    this.url = url;
  }

  private String send(URL url) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    String line = null;
    StringBuilder data = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      data.append(line); 
    }
    return data.toString().trim();
  }	

  public void run() {
    try {
      data = send(url);
    } catch(Throwable t) {
      this.exception = t;
    } finally {
    }
  }

  public String get() {
    if (exception == null)	
      return data;
    throw new RuntimeException(exception);
  }
}

class _02_ParallelAsynchronousUsingThreadsPull {
  public static void main(String[] args) throws Exception {
    // URL placesNearbyUrl = new URL ("http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km");
    URL placesNearbyUrl = new URL("https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km");
    // URL weatherUrl = new URL("http://localhost:8000/weather?lat=19.01&lon=72.8");
    URL weatherUrl = new URL("https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8");
    Request placesNearbyRequest = new Request(placesNearbyUrl);
    Request weatherRequest = new Request(weatherUrl);
    Thread placesNearbyThread = new Thread(placesNearbyRequest);
    Thread weatherThread = new Thread(weatherRequest);	  
    long startTime = System.currentTimeMillis();
    placesNearbyThread.start();
    weatherThread.start();
    // explicit synchronization points
    placesNearbyThread.join();
    weatherThread.join();
    long timeTaken = System.currentTimeMillis() - startTime;
    String placesNearbyData = placesNearbyRequest.get();
    String weatherData = weatherRequest.get();
    String weatherAndPlacesNearby = String.format("{ \"weather\" : %s, \"placesNearby\": %s }", weatherData, placesNearbyData);
    System.out.println(String.format("Time Taken %d(ms)", timeTaken));
    System.out.println(weatherAndPlacesNearby);
  }
}