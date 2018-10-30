using System;
using System.Threading;
using System.Net;
using System.Diagnostics;

struct Results {
  public string weatherData;
  public string nearbyPlacesData;
  public Exception exception;
}

class _02_ParallelAsynchronousUsingThreadPoolPull
{  
  private static void BypassAllCertificates()
  {
    ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain,error) => true;
  }

  private static string Send(string url) {
    using (WebClient wc = new WebClient())
    {
      BypassAllCertificates();
      return wc.DownloadString(url);
    }
  }
  
  public static void Main(string[] args) {
    // string placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    string placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
    // string weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8";
    string weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8";

    using (var latch = new CountdownEvent(2)) {
      var results = new Results();
      var sw = Stopwatch.StartNew();
      ThreadPool.QueueUserWorkItem(url => {
        try {
          results.nearbyPlacesData = Send((string)url);
        } catch (Exception e) {
          results.exception = e;  
        }
        latch.Signal();
      }, placesNearbyUrl);
      
      ThreadPool.QueueUserWorkItem(url => {
        try {
          results.weatherData = Send((string)url);
        } catch (Exception e) {
          results.exception = e;  
        }
        latch.Signal();
      }, weatherUrl);
      // Wait for both tasks to complete
      latch.Wait();  
      sw.Stop();
      Console.WriteLine($"Got Results in {sw.Elapsed.TotalMilliseconds}(ms)");
      // Thread.Sleep(TimeSpan.FromSeconds(1));
      Console.WriteLine($"{{ \"weather\" : {results.weatherData}, \"placesNearby\" :  {results.nearbyPlacesData} }}");
      Console.WriteLine($"Exception = {results.exception}");
    };
  }
}