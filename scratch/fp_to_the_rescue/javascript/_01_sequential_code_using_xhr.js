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

const host = "https://geographic-services.herokuapp.com";
// const host = "https://localhost:8000";
const nearbyPath = "/places/nearby";
const weatherPath = "/weather";
const lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";

const placesNearbyUrl = `${host}${nearbyPath}?${lat}&${lon}&${radius}&${units}`;
const weatherUrl = `${host}${weatherPath}?${lat}&${lon}`;

console.info(weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl));
