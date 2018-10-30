import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration.{Duration}
import scala.util.{Try, Success, Failure}
import scala.async.Async.{async, await}

def getRequestData(url: String): Future[String] = async {
  io.Source.fromURL(url).mkString.trim
} 

// val placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
// val weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
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