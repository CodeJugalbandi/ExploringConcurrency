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
