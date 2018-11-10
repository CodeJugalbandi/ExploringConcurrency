const WebSocket = require('websocket').w3cwebsocket;

function connectTo(websocketUrl, onMessage, onError, onClose) {
	console.log(`WebSocket URL = ${websocketUrl}`);
	const ws = new WebSocket(websocketUrl);
    let ready = false;
	// event emmited when connected
	ws.onopen = function(openMessage) {
	  console.log(`websocket is connected...${openMessage}`)
	  // sending a send event to websocket server
	  ws.send('client ready for communication...');
	  ready = true;
	};
	ws.onclose = closeMessage => onClose(closeMessage);
	ws.onmessage = function(message) {
      try {
        const messageJson = JSON.parse(message.data);
		if (messageJson.ack) {
		  console.info("Received ack => ", messageJson);
		  return;
		}
		if (messageJson.ticker) {
		  onMessage(messageJson);
		  return;
        }
	    if (messageJson.error) {
		  onError(messageJson);
		  return;
	    }
		console.info(messageJson);
	  } catch (e) {
	    console.error(`Could Not call onMessage for ${message.data}`);
        console.debug(`Problem => ${e}`);
	  }
	};
	ws.onerror = error => onError(error);
	
	return {
		close: function () {
			ws.close();
		},
	    unsubscribe: function () {
		  if (ready) {
  			console.info("Client sending 'unsubscribe' command...");
		  	ws.send('unsubscribe');			
		  }
	    },
		subscribe: function () {
  		  if (ready) {
			console.info("Client sending 'subscribe' command...");
  		  	ws.send('subscribe');			
  		  }
		}
	};
}

const ticker = "GOOG";
const websocketUrl = `wss://national-stock-service.herokuapp.com/stocks/realtime/${ticker}`
// const websocketUrl = `wss://localhost:5000/stocks/realtime/${ticker}`

const subscription = connectTo(websocketUrl, 
	message => console.info(message), 
	error => console.info(error), 
	closeMessage => console.info(closeMessage));

setTimeout(() => subscription.subscribe(), 2000);
setTimeout(() => subscription.unsubscribe(), 8000);
setTimeout(() => subscription.close(), 9000);
