import scala.concurrent.ExecutionContext.Implicits.global
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

def weatherAndNearbyPlaces(weatherUrl: String, placesNearbyUrl: String): Future[String] =  
  Future.sequence(List(getRequestData(weatherUrl), getRequestData(placesNearbyUrl)))
    .map (data => {
      val weather::placesNearby::Nil = data
      s"""{ "weather" : $weather, "placesNearby": $placesNearby }"""
    })

// val placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
// val weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
val weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"

val startTime = System.currentTimeMillis()
val request = weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl)
val result = Await.result(request, Duration.Inf) // Wait for results, only for this example, not in practice
val timeTaken = System.currentTimeMillis() - startTime
println(s"Time Taken $timeTaken (ms)")							 
println(result)