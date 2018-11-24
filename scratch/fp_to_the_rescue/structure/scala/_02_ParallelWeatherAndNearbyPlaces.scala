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

val host = "https://geographic-services.herokuapp.com";
// val host = "https://localhost:8000";
val nearbyPath = "/places/nearby"; val weatherPath = "/weather";
val lat = "lat=19.01"; val lon = "lon=72.8"; val radius = "radius=25"; val units = "unit=km";

val placesNearbyUrl = s"$host$nearbyPath?$lat&$lon&$radius&$units";
val weatherUrl = s"$host$weatherPath?$lat&$lon";

val startTime = System.currentTimeMillis()
val request = weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl)
val result = Await.result(request, Duration.Inf) // Wait for results, only for this example, not in practice
val timeTaken = System.currentTimeMillis() - startTime
println(s"Time Taken $timeTaken (ms)")							 
println(result)