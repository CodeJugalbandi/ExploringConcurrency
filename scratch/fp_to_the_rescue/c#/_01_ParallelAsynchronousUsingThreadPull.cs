using System;
using System.Threading;
using System.Net;
using System.Diagnostics;

class Request
{
  private string data;
  private Exception exception;
  private readonly string url;

  public Request(string url)
  {
    this.url = url;
  }

  private void BypassAllCertificates()
  {
    ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain,error) => true;
  }

  public void Send() {
    try {
      using (WebClient wc = new WebClient())
      {
        BypassAllCertificates();
        data = wc.DownloadString(url);
      }
    } catch(Exception e) {
      this.exception = e;
    }
  }

  public String Get() {
    if (exception == null)
      return data;
    throw exception;
  }
}

class _01_ParallelAsynchronousUsingThreadPull {
  
  static Thread CreateRequestThread() {
    return new Thread(request => ((Request)request).Send());
  }

  public static void Main(string[] args) {
    // string placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    string placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    // string weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8";
    string weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8";
    Request placesNearbyRequest = new Request(placesNearbyUrl);
    Request weatherRequest = new Request(weatherUrl);
    var placesNearbyThread = CreateRequestThread();
    var weatherThread = CreateRequestThread();	  
    var sw = new Stopwatch();
    sw.Start();
    placesNearbyThread.Start(placesNearbyRequest);
    weatherThread.Start(weatherRequest);
    // explicit synchronization points
    placesNearbyThread.Join();
    weatherThread.Join();
    sw.Stop();
    string placesNearbyData = placesNearbyRequest.Get();
    string weatherData = weatherRequest.Get();
    string weatherAndPlacesNearby = $"{{ \"weather\" : {weatherData}, \"placesNearby\": {placesNearbyData} }}";
    Console.WriteLine($"Time Taken {sw.Elapsed.TotalMilliseconds}(ms)");
    Console.WriteLine(weatherAndPlacesNearby);
  }
}