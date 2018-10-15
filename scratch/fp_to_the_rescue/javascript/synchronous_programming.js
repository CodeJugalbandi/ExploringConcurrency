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

// const weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
// const placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"

console.time('Time Taken'); // starts the timer
getRequestData(weatherUrl, (werror, weather) => {
  if (werror) {
    throw new Error(`Request Failed ${error.message}`);
  }
  getRequestData(placesNearbyUrl, (perror, placesNearby) => {
	if (perror) {
	  throw new Error(`Request Failed ${error.message}`);
	}
	console.timeEnd('Time Taken'); // End the timer, get the elapsed time
    const result = JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`);
	console.info(result);
  });
});

