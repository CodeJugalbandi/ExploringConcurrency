using System;
using System.Threading;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Collections.Generic;
using System.Net;

class _03_ParallelUsingTPL
{
  public static void Main(string[] args) {
    string host  = "https://geographic-services.herokuapp.com";
    // string host  = "https://localhost:8000";
    string nearbyPath = "/places/nearby", weatherPath = "/weather";
    string lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";
    
    string placesNearbyUrl = $"{host}{nearbyPath}?{lat}&{lon}&{radius}&{units}";
    string weatherUrl = $"{host}{weatherPath}?{lat}&{lon}";
    var sw = Stopwatch.StartNew();
    var placesNearby = MakeRequest(placesNearbyUrl);
    var weather = MakeRequest(weatherUrl);
    var t = Task.WhenAll(placesNearby, weather);
    try {
      t.Wait();
      string placesNearbyData = t.Result[0];
      string weatherData = t.Result[1];
      Console.WriteLine($"{{ \"weather\" : {weatherData}, \"placesNearby\" : {placesNearbyData} }}");
    } catch (AggregateException ae) {
      var exceptions = ae.Flatten().InnerExceptions;
      Console.WriteLine($"Exceptions caught: {exceptions.Count}");
      foreach (var e in exceptions) {
        Console.WriteLine($"Exception details =====> {e}");
        Console.WriteLine();
      }
    }
    sw.Stop();
    Console.WriteLine($"Time Taken {sw.Elapsed.TotalMilliseconds}");
    Console.WriteLine("DONE");
  }
  
  static Task<string> MakeRequest(string url) {
    return Task<string>.Run(() => Send((string)url));
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
