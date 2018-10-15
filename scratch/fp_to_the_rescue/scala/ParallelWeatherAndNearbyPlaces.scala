import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Failure, Success}
import scala.concurrent.{Future, Promise}

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

// val placesNearbyUrl = s"http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = s"https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
// val weatherUrl = s"http://localhost:8000/weather?lat=19.01&lon=72.8"
val weatherUrl = s"https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"

val startTime = System.currentTimeMillis()
val placesNearbyData = getRequestData(placesNearbyUrl)
val weatherData = getRequestData(weatherUrl)
val weatherAndPlacesNearby = for {
  placesNearby <- placesNearbyData
  weather <- weatherData
} yield s"""{ "weather" : $weather, "placesNearby": $placesNearby }"""

weatherAndPlacesNearby.onComplete {
  case Success(data) => {
	val timeTaken = System.currentTimeMillis() - startTime
	println(s"Time Taken $timeTaken (ms)")							 
    println(data)
  }
  case Failure(ex) => println(ex.getMessage())
}

Thread.sleep(8000)