# Functional Programming to the Rescue

In this melody, we will look at how FP rescues us from the pitfalls by shifting our focus on to the "what" than on to the "how".  We will contrast how it simplifies the approach to concurrency and parallelism by raising the level of abstraction to a point where the shift in focus on the code is achieved at a conceptual domain level, rather than worrying about the associated plumbing (and boiler-plate) to get there.  

Also, etymologically the structure of concurrent code has been different from the structure of sequential code.  But, we shall see how FP can be leveraged to provide a syntactic layer with async-await using a library or within a language  to make the structure of sequential code and parallel code look similar.

**BRAHMA** We will demonstrate this using problem statement - Given a latitude and longitude of a place, we want to get the following details:

* Weather information (from a weather end-point).
* Nearby Places with a radius of 25 Kms (from a places service).

**KRISHNA** The sequential code for this is too simple to write.  Lets pen this in Scala.

```scala
def getRequestData(url: String): String = io.Source.fromURL(url).mkString.trim

lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = s"https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val weatherUrl = s"https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"

val startTime = System.currentTimeMillis()
val placesNearbyData = getRequestData(placesNearbyUrl)
val weatherData = getRequestData(weatherUrl)
val timeTaken = System.currentTimeMillis() - startTime
val weatherAndPlacesNearby = s"""{ "weather" : $weatherData, "placesNearby": $placesNearbyData }"""
println(s"Time Taken $timeTaken (ms)")							 
println(weatherAndPlacesNearby)
```

**BRAHMA** We can make this parallel and speed-up the execution.  Lets use  thread primitives to begin with.  We need to spawn two threads, wait for each of them to finish and then gather the partial results to get complete results.  Lets see this in C#

```csharp
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

class ParallelAsynchronousUsingThread 
{
  
  static Thread CreateRequestThread() 
  {
    return new Thread(request => ((Request)request).Send());
  }

  public static void Main(string[] args) 
  {
    string placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
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

```
**BRAHMA** But we know that threads are a scarce resource and they cannot be re-used.  So, creating ad-hoc threads like this is not advisable.  So, we use ThreadPool to re-use threads, put it back into the pool after they are used for a task and thus help with thread management.

```csharp
using System;
using System.Threading;
using System.Net;
using System.Diagnostics;

struct Results {
  public string weatherData;
  public string nearbyPlacesData;
  public Exception exception;
}

class ParallelAsynchronousUsingThreadPool
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
    string placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";
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
      Console.WriteLine($"{{ \"weather\" : {results.weatherData}, \"placesNearby\" :  {results.nearbyPlacesData} }}");
      Console.WriteLine($"Exception = {results.exception}");
    };
  }
}
```

**KRISHNA** Again, if you look the code it to verbose and we have to manage the synchronization point, in this case using a ```CountdownEvent```.  A slight improvement over the earlier one.  Further we also deal with  ```Threadpool``` to queue the task.  Let me show you Clojure, where all this boiler-plate is shoved under an abstraction ```future``` that deals with all of this.

```clojure
(ns weather-and-places)

(def http-host "https://geographic-services.herokuapp.com")
(def nearby-path "/places/nearby")
(def weather-path "/weather")

(defn nearby-url [lat lon radius unit]
  (format "%s%s?lat=%s&lon=%s&radius=%s&unit=%s"
          http-host nearby-path lat lon radius unit))

(defn weather-url [lat lon]
  (format "%s%s?lat=%s&lon=%s"
          http-host weather-path lat lon))

(defn print-info [weather places]
  (println
    (format "{ \"weather\": %s, \"placesNearby\": %s }"
            weather places)))

(def test-lat "19.01")
(def test-lon "72.8")
(def radius 25)
(def units "km")

(time
      (let [weather (future (slurp (weather-url test-lat test-lon)))
            places (future (slurp (nearby-url test-lat test-lon radius units)))]

       (print-info @weather @places)))

(shutdown-agents)
```

**KRISHNA** So, here there is no explicit synchronization point and threadpool that we manage.  The ```future``` itself is backed by a thread from the threadpool and we simply call ```shutdown-agents``` in the end to close it.  Due to this, the code is deviod of boilerplate, bringing out the essence of domain to the fore, than being muddled within boilerplate.  The ```future``` is a higher order function that provides asynchronous computation unit.

**BRAHMA** However, if we look all this we are still pull based - the @ deref operator.  It would be better if we had Push based, so that we get called when the latent tasks finish, instead of blocking the main thread to gather partial results.  Lets look at JavaScript.

```javascript
const request = require('request');

function getRequestData(url) {
  return new Promise((resolve, reject) => {
    request.get(url, (error, response, body) => {
      if (error) {
        reject(error);
      } else {
        resolve(body);
      }
    });
  });
}

function weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl) {
  console.time('Time Taken');
  return Promise.all([getRequestData(weatherUrl), getRequestData(placesNearbyUrl)])
    .then(([weather, placesNearby]) => {
      console.timeEnd('Time Taken');
      return JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`)
    })
    .catch(error => {
      console.timeEnd('Time Taken');
      return JSON.parse(`{ "error": "Request Failed ${error}" }`);
    });
}

const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"

weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl)
  .then(result => console.info(result));
```

**BRAHMA** So, here the function ```getRequestData ``` returns a ```Promise```.  It is a Monad for doing asynchronous computation.  If we get a response from the end-point, we fulfill the promise using a ```resolve()``` call, and in case of error we break the promise - a.k.a ```reject()```.  In the function ```weatherAndNearbyPlaces``` we consume the URLs, create promises for each of them and using ```Promise.all``` we combine all the promises and set-up a pipeline of computations that follow.  If all the promises within succeed, the ```then``` gets executed - herein we destructure the result and map it to response JSON.   In case, if either of the promises fails, the ```catch``` in the pipeline is executed and we map it to error JSON.  So, this is Push and the data-flows through the transformation pipeline.

**KRISHNA** So, far so good.  But we have observed that the structure of the sequential code is quite different from that of the concurrent code.

**BRAHMA** Yes indeed, but languages like JavaScript/C#, the language has evolved syntactic layer with async-await constructs which underneath use these abstractions.  In Scala too, we have the async library that does that job.  Since we've looked at the earlier sequential example in Scala, lets see it in that.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration.{Duration}
import scala.async.Async.{async, await}

def getRequestData(url: String): Future[String] = async {
  io.Source.fromURL(url).mkString.trim
} 

val placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"

val startTime = System.currentTimeMillis()
val weatherAndPlacesNearby = async {
  val weatherFuture = getRequestData(weatherUrl)
  val placesNearbyFuture = getRequestData(placesNearbyUrl)
  s"""{ "weather" : ${await(weatherFuture)}, "placesNearby" : ${await(placesNearbyFuture)} }"""
}
println(Await.result(weatherAndPlacesNearby, Duration.Inf))
val timeTaken = System.currentTimeMillis() - startTime
println(s"Time Taken $timeTaken (ms)")
```

**BRAHMA** Now, the structure is quite similar, we simply wrap things in async and await construct and we are still not changing our thought process.  In Scala, we could also have used for-comprehensions, but they are more generic than the async-await.

TODO: **VISHNU** Lets cross over to Array-Oriented Paradigm, in APL, we too use Futures.

## Reflections

**WIP =>**

In the early days, we have used Threads to achieve concurrency and parallelism. But we soon realized that it is too difficult to work at this level of abstraction, due to many reasons.  ThreadPool got introduced to simplify Thread management.  Also, the data-Push was more concretized.  However the other problems still remained unaddressed.  In the main-stream imperative and OO languages like C#, Java, C++,
