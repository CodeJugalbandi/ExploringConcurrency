import java.io.*;
import java.net.*;

import org.json.JSONObject;

public class NationalStockService {
  private final String urlTemplate = "http://national-stock-service.herokuapp.com:5000/stocks/%s";

  public double getPrice(final String ticker) throws Exception {
    System.out.println(Thread.currentThread() + " Getting Price for => " + ticker);
    final URL url = new URL(String.format(urlTemplate, ticker));
    final String data = getData(url.openConnection());
    final JSONObject stockDetails = new JSONObject(data);
    double price = stockDetails.getDouble("price");
    System.out.println(Thread.currentThread() + " Returning Price for => " + ticker + " price = " + price);
    return price;
  }

  private String getData(final URLConnection connection) throws IOException {
    InputStream is = connection.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader reader = new BufferedReader(isr);
    String line = null;
    StringBuilder data = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      data.append(line);
    }
    return data.toString();
  }
}