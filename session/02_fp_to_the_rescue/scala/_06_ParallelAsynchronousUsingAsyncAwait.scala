import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration.{Duration}
import scala.util.{Try, Success, Failure}
import scala.async.Async.{async, await}

def getRequestData(url: String): Future[String] = async {
  io.Source.fromURL(url).mkString.trim
} 

val host = "http://geographic-services.herokuapp.com:8000";
val nearbyPath = "/places/nearby"; val weatherPath = "/weather";
val lat = "lat=19.01"; val lon = "lon=72.8"; val radius = "radius=25"; val units = "unit=km";

val placesNearbyUrl = s"$host$nearbyPath?$lat&$lon&$radius&$units";
val weatherUrl = s"$host$weatherPath?$lat&$lon";

val startTime = System.currentTimeMillis
val weatherAndPlacesNearby = async {
  val weatherFuture = getRequestData(weatherUrl)
  val placesNearbyFuture = getRequestData(placesNearbyUrl)
  s"""{ "weather" : ${await(weatherFuture)}, "placesNearby" : ${await(placesNearbyFuture)} }"""
}

weatherAndPlacesNearby.onComplete {
  case Success(data) => {
    val timeTaken = System.currentTimeMillis - startTime
    println(s"Time Taken $timeTaken (ms)")
    println(data)
  }
  case Failure(e) => println(e.getMessage)
}

Await.result(weatherAndPlacesNearby, Duration.Inf) // Wait for results, only for this example for onComplete to get called, not in practice
