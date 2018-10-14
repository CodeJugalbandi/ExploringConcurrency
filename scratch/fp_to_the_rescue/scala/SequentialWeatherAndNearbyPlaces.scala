import scala.util.parsing.json._
import scala.util.Try

val startTime = System.currentTimeMillis()
// val placesNearbyUrl = s"http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = s"https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearby = io.Source.fromURL(placesNearbyUrl).mkString.trim
// val weatherUrl = s"http://localhost:8000/weather?lat=19.01&lon=72.8"
val weatherUrl = s"https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
val weather = io.Source.fromURL(weatherUrl).mkString.trim
val timeTaken = System.currentTimeMillis() - startTime
val weatherAndPlacesNearby = s"""
                               | {
							   |   "weather"     : $weather,
							   |   "placesNearby": $placesNearby
							   | }
                              """.stripMargin
println(s"Time Taken $timeTaken (ms)")							 
println(weatherAndPlacesNearby)
// println(JSON.parseFull(weatherAndPlacesNearby))