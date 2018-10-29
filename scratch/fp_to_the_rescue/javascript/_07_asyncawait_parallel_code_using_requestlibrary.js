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

async function all(urls) {
  const promises = urls.map(async url => await getRequestData(url));
  return await Promise.all(promises); // wait until all promises resolve
}

async function weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl) {
  console.time('Time Taken');
  try {
    const [weather, placesNearby] = await all([weatherUrl, placesNearbyUrl]);
    console.timeEnd('Time Taken');
    return JSON.parse(`{ "weather": ${weather}, "placesNearby": ${placesNearby} }`);
  } catch(e) {
    console.timeEnd('Time Taken');
    return JSON.parse(`{ "error": "Request Failed ${e.message}" }`);
  }
}

// const weatherUrl = "http://localhost:8000/weather?lat=19.01&lon=72.8"
const weatherUrl = "https://geographic-services.herokuapp.com/weather?lat=19.01&lon=72.8";
// const placesNearbyUrl = "http://localhost:8000/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km"
const placesNearbyUrl = "https://geographic-services.herokuapp.com/places/nearby?lat=19.01&lon=72.8&radius=25&unit=km";


weatherAndNearbyPlaces(weatherUrl, placesNearbyUrl)
 .then(result => console.info(result));
