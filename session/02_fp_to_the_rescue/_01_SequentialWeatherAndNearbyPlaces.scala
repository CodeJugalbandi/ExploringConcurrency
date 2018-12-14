def getRequestData(url: String): String = io.Source.fromURL(url).mkString.trim

val host = "http://geographic-services.herokuapp.com:8000";
val nearbyPath = "/places/nearby"; val weatherPath = "/weather";
val lat = "lat=19.01"; val lon = "lon=72.8"; val radius = "radius=25"; val units = "unit=km";

val placesNearbyUrl = s"$host$nearbyPath?$lat&$lon&$radius&$units";
val weatherUrl = s"$host$weatherPath?$lat&$lon";

val placesNearbyData = getRequestData(placesNearbyUrl)
val weatherData = getRequestData(weatherUrl)
val weatherAndPlacesNearby = s"""{ "weather" : $weatherData, "placesNearby": $placesNearbyData }"""
println(weatherAndPlacesNearby)