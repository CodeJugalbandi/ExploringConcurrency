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

// const weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
// const placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"

weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl, (error, response) => {
  if (error)
    console.error(error);
  else 
    console.info(response);
});