const request = require('request');

function getRequestData(url, cb) {
  return request.get(url, (error, response, body) => {
    if (error) {
      cb(error, null);
	  } else {
      cb(null, body);
	  }
  });
}

function weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl, cb) {
  console.time('Time Taken'); // starts the timer
  getRequestData(weatherUrl, (werror, weather) => {
    if (werror) {
	    console.timeEnd('Time Taken');
      const cause = JSON.parse(`{ "error" : "Request Failed ${werror.message}" }`);
      cb(cause, null);
      return;
    }
    getRequestData(placesNearbyUrl, (perror, placesNearby) => {
	    if (perror) {
  	    console.timeEnd('Time Taken');
        const cause = JSON.parse(`{ "error" : "Request Failed ${werror.message}" }`);
        cb(cause, null);
        return;
	    }
	    console.timeEnd('Time Taken'); 
      const result = JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`);
	    cb(null, result);
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