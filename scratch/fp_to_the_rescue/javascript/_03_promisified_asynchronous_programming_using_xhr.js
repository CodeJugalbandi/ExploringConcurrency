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

// const weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8"
// const placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"

weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl)
  .then(result => console.info(result));
