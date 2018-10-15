import java.net.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

@FunctionalInterface
interface SupplierThrowsException<T, E extends Throwable> {
  T get() throws E;
}

class ParallelAsynchronousUsingCompleteableFutureGeneralPurpose {
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
  
  private static <T, E extends Throwable> CompletableFuture<T> supplyAsync(SupplierThrowsException<T, E> ste) {
	return CompletableFuture.supplyAsync(() -> {
      try {
        return ste.get();	
      } catch (Throwable t) {
	    throw new RuntimeException(t);
      }
	});
  }
  
  private static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> cfs) {
    CompletableFuture<List<T>> future = new CompletableFuture<>();
    future.runAsync(() -> {
      try {
        future.complete(cfs.stream().map(cf -> cf.join()).collect(Collectors.toList()));  
      } catch (Throwable t) {
        future.completeExceptionally(t);
      }
    });
    return future;
  }

  public static void main(String[] args) throws Exception {
    // String placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    String placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    // String weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8";
    String weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8";
	 
    long startTime = System.currentTimeMillis();
	CompletableFuture<String> placesNearby = supplyAsync(() -> getRequestData(placesNearbyUrl));
    CompletableFuture<String> weather = supplyAsync(() -> getRequestData(weatherUrl));
	CompletableFuture<List<String>> results = all(List.of(placesNearby, weather));
	results.handle((data, throwable) -> {
	      if (data != null) {
	    String placesNearbyData = data.get(0);
	    String weatherData = data.get(1);
	    return String.format("{ \"weather\" : %s, \"placesNearby\": %s }", weatherData, placesNearbyData);
	      }
	  return throwable.getMessage();
	}).thenAccept(result -> {
	  long timeTaken = System.currentTimeMillis() - startTime;
	  System.out.println(String.format("Time Taken %d(ms)", timeTaken));
	  System.out.println(result);
	});
	
  }
}