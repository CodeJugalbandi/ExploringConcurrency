using System;
using System.Threading;
using System.Net;
using System.Diagnostics;

class _01_ParallelAsynchronousUsingThreadPull {
  
  static Thread MakeRequestThread() {
    return new Thread(request => ((Request)request).Send());
  }

  public static void Main(string[] args) {
    string host  = "https://geographic-services.herokuapp.com";
    // string host  = "https://localhost:8000";
    string nearbyPath = "/places/nearby", weatherPath = "/weather";
    string lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";
    
    string placesNearbyUrl = $"{host}{nearbyPath}?{lat}&{lon}&{radius}&{units}";
    string weatherUrl = $"{host}{weatherPath}?{lat}&{lon}";
    Request placesNearby = new Request(placesNearbyUrl);
    Request weather = new Request(weatherUrl);
    var placesNearbyRequestThread = MakeRequestThread();
    var weatherRequestThread = MakeRequestThread();	  
    var sw = new Stopwatch();
    sw.Start();
    placesNearbyRequestThread.Start(placesNearby);
    weatherRequestThread.Start(weather);
    // explicit synchronization points
    placesNearbyRequestThread.Join();
    weatherRequestThread.Join();
    sw.Stop();
    string placesNearbyData = placesNearby.Get();
    string weatherData = weather.Get();
    string weatherAndPlacesNearby = $"{{ \"weather\" : {weatherData}, \"placesNearby\": {placesNearbyData} }}";
    Console.WriteLine($"Time Taken {sw.Elapsed.TotalMilliseconds}(ms)");
    Console.WriteLine(weatherAndPlacesNearby);
  }
}

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