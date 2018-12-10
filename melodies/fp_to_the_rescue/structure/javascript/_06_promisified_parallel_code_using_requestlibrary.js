const request = require('request');

function getRequestData(url) {
  return new Promise((resolve, reject) => {
    request.get(url, (error, response, body) => {
      if (error) {
        reject(error);
      } else {
        resolve(body);
      }
    });
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