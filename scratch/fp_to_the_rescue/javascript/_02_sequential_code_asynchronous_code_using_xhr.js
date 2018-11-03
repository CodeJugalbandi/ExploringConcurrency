const XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;

function getRequestData(url, cb) {
  const request = new XMLHttpRequest();
  request.open('GET', url, true);  // `true` makes the request asynchronous
  request.onreadystatechange = function (e) {
    if (request.readyState === 4) {
      if (request.status === 200) {
        cb(null, request.responseText);
      } else {
        cb(request.statusText, null);
      }
    }
  };
  request.onerror = function(e) {
    const error = request.statusText;
    const cause = `${error.code} ${error.syscall} ${error.hostname}:${error.port}`;
    cb(cause, null);
  };
  request.send();
}

function weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl, cb) {
  console.time('Time Taken'); // starts the timer
  getRequestData(weatherUrl, (werror, weather) => {
    if (werror) {
      const cause = JSON.parse(`{ "error" : "Request Failed ${werror}" }`);
      cb(cause, null);
      return;
    } 
    getRequestData(placesNearbyUrl, (perror, placesNearby) => {
      if (perror) {
        const cause = JSON.parse(`{ "error" : "Request Failed ${perror}" }`);
        cb(cause, null);
        return;
      } 
      console.timeEnd('Time Taken');
      const result = JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`);
      cb(null, result);
      return;
    });
  });
}

const host = "https://geographic-services.herokuapp.com";
// const host = "https://localhost:8000";
const nearbyPath = "/places/nearby";
const weatherPath = "/weather";
const lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";

const placesNearbyUrl = `${host}${nearbyPath}?${lat}&${lon}&${radius}&${units}`;
const weatherUrl = `${host}${weatherPath}?${lat}&${lon}`;

weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl, (error, response) => {
  if (error)
    console.error(error);
  else 
    console.info(response);
});
