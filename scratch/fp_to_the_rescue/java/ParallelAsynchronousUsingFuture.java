import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class ParallelAsynchronousUsingFuture {
  private static String getRequestData(String urlStr) throws MalformedURLException, IOException {
    URL url = new URL(urlStr);
    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    String line = null;
    StringBuilder data = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      data.append(line); 
    }
    return data.toString().trim();
  }

  public static void main(String[] args) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
      }
    });
    // String placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    String placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    // String weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8";
    String weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8";
	 
    long startTime = System.currentTimeMillis();
    List<Future<String>> results = pool.invokeAll(List.of(() -> getRequestData(placesNearbyUrl), () -> getRequestData(weatherUrl)));
    long timeTaken = System.currentTimeMillis() - startTime;
    Future<String> placesNearbyData = results.get(0);
    Future<String> weatherData = results.get(1);
    String weatherAndPlacesNearby = String.format("{ \"weather\" : %s, \"placesNearby\": %s }", weatherData.get(), placesNearbyData.get());
    System.out.println(String.format("Time Taken %d(ms)", timeTaken));
    System.out.println(weatherAndPlacesNearby);
  }
}