const rx = require('rxjs');
const { webSocket } = require('rxjs/webSocket');
const ticker = "";
const websocketUrl = `wss://national-stock-service.herokuapp.com/stocks/realtime/${ticker}`
// const websocketUrl = `wss://localhost:5000/stocks/realtime/${ticker}`

const ws = webSocket({
  url: websocketUrl,
  WebSocketCtor: require('websocket').w3cwebsocket,
	serializer: value => value //override default JSON stringifier.
});

ws.subscribe(
   data => console.log("Data =>", data),
   err => console.error("Error => ", err),
   () => console.warn('*** DONE ****')
);

// Send to Server
setTimeout(() => {
  console.info("Sending subscribe...");
  ws.next('client ready for communication...');
  ws.next('subscribe');
}, 4000);

setTimeout(() => {
  console.info("Sending unsubscribe...");
  ws.next('unsubscribe');
}, 16000);

setTimeout(() => {
  console.info("Sending complete...this closes the connection");
  ws.complete(); //closes the connection
}, 17000);

// setTimeout(() => {
//   console.info("Sending error...this also closes the connection, ");
//   console.info("but lets the server know that this closing is due to some error");
//   ws.error({code: 4000, reason: 'App broke'}); //closes the connection
// }, 18000);
