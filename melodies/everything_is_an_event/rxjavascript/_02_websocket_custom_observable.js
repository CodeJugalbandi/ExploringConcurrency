const rx = require('rxjs');
const { take, share } = require('rxjs/operators');
const WebSocket = require('websocket').w3cwebsocket;

function connectTo(websocketUrl) {
	return new rx.Observable(observer => {
		console.log(`WebSocket URL = ${websocketUrl}`);
		const ws = new WebSocket(websocketUrl);
		
	    const unsubscribeAndClose = () => {
		  if (ws.readyState == 1) {
			  console.info("unsubscribeAndClose...");
			  ws.send("{ \"command\" : \"unsubscribe\" }");
			  ws.close();
		  }
	    };
		
		// event emited when connected
		ws.onopen = function(openMessage) {
		  console.log(`websocket is connected...${openMessage}`)
		  // sending a send event to websocket server
		  ws.send("{ \"command\" : \"subscribe\" }");
		};
		
		ws.onclose = closeMessage => observer.complete();
		ws.onmessage = function(message) {
	      try {
	        const messageJson = JSON.parse(message.data);
			if (messageJson.ack) {
			  console.info("Received ack => ", messageJson);
			  return;
			}
			if (messageJson.ticker) {
			  observer.next(messageJson);
			  return;
	        }
		    if (messageJson.error) {
			  unsubscribeAndClose();
			  observer.error(messageJson);
			  return;
		    }
			console.info(messageJson);
		  } catch (e) {
			unsubscribeAndClose();
			observer.error(e);
		  }
		};
		ws.onerror = error => observer.error(error);
	   
	   // on unsubscribe
	   return () => {
		 unsubscribeAndClose();
	     observer.complete();
	   };
	})
	.pipe(share());
}

const ticker = "";
const websocketUrl = `wss://national-stock-service.herokuapp.com/stocks/realtime/${ticker}`
// const websocketUrl = `wss://localhost:5000/stocks/realtime/${ticker}`

const subscription = connectTo(websocketUrl)
  .pipe(take(3))
  .subscribe(
	next => console.info(next),
	error => console.info(error),
	() => console.info("*** DONE ***")
  );

setTimeout(() => subscription.unsubscribe(), 8000);


