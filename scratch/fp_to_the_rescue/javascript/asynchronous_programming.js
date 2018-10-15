const request = require('request');

async function getRequestData(url) {
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

// const weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
// const placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"

console.time('Time Taken'); 
Promise.all([getRequestData(weatherUrl), getRequestData(placesNearbyUrl)])
  .then(([weather, placesNearby]) => JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`))
  .then(result => {
     console.timeEnd('Time Taken');
	 console.info(result);
  })
  .catch(error => `Could not get data ${error.message}`);
  
