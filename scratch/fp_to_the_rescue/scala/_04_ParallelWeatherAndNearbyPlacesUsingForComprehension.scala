import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Failure, Success}
import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration.{Duration}

def getRequestData(url: String): Future[String] = {
  val p = Promise[String]()
  Future {
    try {
      p.success(io.Source.fromURL(url).mkString.trim)	 	
    } catch {
      case e: Throwable => p.failure(e)
    }
  }
  p.future	
} 

// val placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
// val weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
val weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"

val startTime = System.currentTimeMillis()
val placesNearbyFuture = getRequestData(placesNearbyUrl)
val weatherFuture = getRequestData(weatherUrl)
val request = for {
  placesNearby <- placesNearbyFuture
  weather <- weatherFuture
} yield s"""{ "weather" : $weather, "placesNearby": $placesNearby }"""

val result = Await.result(request, Duration.Inf) // Wait for results, only for this example, not in practice
val timeTaken = System.currentTimeMillis() - startTime
println(s"Time Taken $timeTaken (ms)")							 
println(result)