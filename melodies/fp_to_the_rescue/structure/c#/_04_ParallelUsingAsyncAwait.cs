using System;
using System.Threading;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Collections.Generic;
using System.Net;

class _04_ParallelUsingAsyncAwait
{
  public static void Main(string[] args) {
    string host  = "https://geographic-services.herokuapp.com";
    // string host  = "https://localhost:8000";
    string nearbyPath = "/places/nearby", weatherPath = "/weather";
    string lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";
    
    string placesNearbyUrl = $"{host}{nearbyPath}?{lat}&{lon}&{radius}&{units}";
    string weatherUrl = $"{host}{weatherPath}?{lat}&{lon}";
    var sw = Stopwatch.StartNew();
    string result = WeatherAndNearbyPlacesAsync(placesNearbyUrl, weatherUrl).Result;
    sw.Stop();
    Console.WriteLine($"{result}");
    Console.WriteLine($"Time Taken {sw.Elapsed.TotalMilliseconds}");
    Console.WriteLine("DONE");
  }
  
  static async Task<string> WeatherAndNearbyPlacesAsync(string placesNearbyUrl, string weatherUrl) {
    var placesNearby = MakeRequestAsync(placesNearbyUrl);
    var weather = MakeRequestAsync(weatherUrl);
    try {
      return $"{{ \"weather\" : {await weather}, \"placesNearby\" : {await placesNearby} }}";
    } catch (Exception e) {
      return $"{{ \"error\": \"{e.Message}\" }}";
    }
  }
  
  static Task<string> MakeRequestAsync(string url) {
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
