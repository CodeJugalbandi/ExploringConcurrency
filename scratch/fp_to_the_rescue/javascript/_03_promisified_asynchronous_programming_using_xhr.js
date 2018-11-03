const XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;

function getRequestData(url) {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open('GET', url, true);  // `true` makes the request asynchronous
    request.onreadystatechange = function () {
      if (request.readyState === 4) {
        if (request.status === 200) {
          resolve(request.responseText);
        } else {
          reject(request.statusText);
        }
      }
    };
    request.onerror = function (e) {
      const error = request.statusText;
      const cause = `"${error.code} ${error.syscall} ${error.hostname}:${error.port}"`;
      reject(cause);
    };
    request.send();
  });
}

function weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl) {
  console.time('Time Taken');
  return Promise.all([getRequestData(weatherUrl), getRequestData(placesNearbyUrl)])
    .then(([weather, placesNearby]) => {
      console.timeEnd('Time Taken');
      return JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`)
    })
    .catch(error => {
      console.timeEnd('Time Taken');
      return JSON.parse(`{ "error": "Request Failed ${error}" }`);
    });
}

const host = "https://geographic-services.herokuapp.com";
// const host = "https://localhost:8000";
const nearbyPath = "/places/nearby";
const weatherPath = "/weather";
const lat = "lat=19.01", lon = "lon=72.8", radius = "radius=25", units = "unit=km";

const placesNearbyUrl = `${host}${nearbyPath}?${lat}&${lon}&${radius}&${units}`;
const weatherUrl = `${host}${weatherPath}?${lat}&${lon}`;

weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl)
  .then(result => console.info(result));
