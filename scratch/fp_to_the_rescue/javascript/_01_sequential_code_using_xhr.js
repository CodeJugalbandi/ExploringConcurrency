const XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;

function getRequestData(url) {
  const request = new XMLHttpRequest();
  request.open('GET', url, false);  // `false` makes the request synchronous
  request.send();
  if (request.readyState === 4) {
    if (request.status === 200) {
      return request.responseText;
    } else {
      const error = request.statusText;
      throw new Error(`${error.code} ${error.syscall} ${error.hostname}:${error.port}`);
    }
  }
}

function weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl) {
  console.time('Time Taken'); // starts the timer
  try {
    const weather = getRequestData(weatherUrl);
    const placesNearby = getRequestData(placesNearbyUrl);
    console.timeEnd('Time Taken'); // End the timer, get the elapsed time
    return JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`);
  } catch (e) {
    console.timeEnd('Time Taken'); // End the timer, get the elapsed time
    return JSON.parse(`{ "error": "Request Failed ${e.message}" }`);
  }
}

// const weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
// const placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"

console.info(weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl));
