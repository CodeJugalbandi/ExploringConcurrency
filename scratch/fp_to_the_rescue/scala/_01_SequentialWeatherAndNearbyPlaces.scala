def getRequestData(url: String): String = io.Source.fromURL(url).mkString.trim

// val placesNearbyUrl = s"http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
val placesNearbyUrl = s"https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
// val weatherUrl = s"http://localhost:8000/weather?lat=19.01&lon=72.8"
val weatherUrl = s"https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"

val startTime = System.currentTimeMillis()
val placesNearbyData = getRequestData(placesNearbyUrl)
val weatherData = getRequestData(weatherUrl)
val timeTaken = System.currentTimeMillis() - startTime
val weatherAndPlacesNearby = s"""{ "weather" : $weatherData, "placesNearby": $placesNearbyData }"""
println(s"Time Taken $timeTaken (ms)")							 
println(weatherAndPlacesNearby)
