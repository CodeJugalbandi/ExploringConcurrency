using System;
using System.Threading;
using System.Net;
using System.Diagnostics;

class _02_ParallelAsynchronousUsingThreadPoolPull
{  
  public static void Main(string[] args) {
    string host  = "http://geographic-services.herokuapp.com:8000";
    string nearbyPath = "/places/nearby", weatherPath = "/weather";
    string lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";
    
    string placesNearbyUrl = $"{host}{nearbyPath}?{lat}&{lon}&{radius}&{units}";
    string weatherUrl = $"{host}{weatherPath}?{lat}&{lon}";
    using (var latch = new CountdownEvent(2)) {
      var sw = Stopwatch.StartNew();
      var placesNearbyResult = MakeRequest(placesNearbyUrl, latch);
      var weatherResult = MakeRequest(weatherUrl, latch);
      // Wait for both tasks to complete
      latch.Wait();  
      sw.Stop();
      Console.WriteLine($"Got Results in {sw.Elapsed.TotalMilliseconds}(ms)");
      Console.WriteLine($"{{ \"weather\" : {weatherResult.data}, \"placesNearby\" : {placesNearbyResult.data} }}");
      Console.WriteLine($"{{ \"error\" : \"{weatherResult.exception}{placesNearbyResult.exception}\" }}");
    };
  }
  
  static Result MakeRequest(string url, CountdownEvent latch) {
    var result = new Result();
    ThreadPool.QueueUserWorkItem(_ => {
      try {
        result.data = Send((string)url);
      } catch (Exception e) {
        result.exception = e;  
      }
      latch.Signal();    
    });
    return result;
  }
  
  private static string Send(string url) {
    using (WebClient wc = new WebClient())
    {
      BypassAllCertificates();
      return wc.DownloadString(url);
    }
  }
  
  private static void BypassAllCertificates()
  {
    ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain,error) => true;
  }
}

class Result {
  public string data;
  public Exception exception;
}
