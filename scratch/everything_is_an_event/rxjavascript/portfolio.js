const rx = require('rxjs');
const { concatMap, delay, tap, map, filter, scan, take, share } = require('rxjs/operators');

class Portfolio {
  constructor() {
    this.stocks = new Map();
  }
  add(ticker, qty) {
    const oldQty = 0;
    if (this.stocks.has(ticker)) {
      oldQty = this.stocks.get(ticker);
    }
    this.stocks.set(ticker, oldQty + qty);
  }
  netWorth(tickers) {
    return tickers.pipe(
      filter(tick => this.stocks.has(tick.ticker)),
      scan((acc, tick) => {
        const ticker = tick["ticker"];
        acc.set(ticker, this.stocks.get(ticker) * tick["price"]);
        return acc;
      }, new Map()),
      map(worth => [ ... worth.values()].reduce((a, e) => a + e), 0));
  }
}

const portfolio = new Portfolio();
portfolio.add("GOOG", 10);
portfolio.add("AAPL", 20);
portfolio.add("YHOO", 30);
portfolio.add("MSFT", 40);

const { webSocket } = require('rxjs/webSocket');

const ticker = "";
const websocketUrl = `wss://national-stock-service.herokuapp.com/stocks/realtime/${ticker}`
// const websocketUrl = `ws://localhost:5000/stocks/realtime/${ticker}`

const ws = webSocket({
  url: websocketUrl,
  WebSocketCtor: require('websocket').w3cwebsocket,
});

const tickers = ws.pipe(
  tap(message => console.info(message)),
  filter(json => json["ticker"]),
  share());

// add 2% brokerage to every price that the user sees.
const brokerage = 0.02;
tickers.pipe(
  tap(tick => console.info("Before Brokerage  => " + tick["price"])),
  map(message => {
    const brokeredPrice = message["price"] * (1 + brokerage);
    message["price"] = brokeredPrice;
    return message;
  }))
  .subscribe(tick => console.info("After Brokerage Price => " + tick["price"]),
    error => console.error("Price Error => " + error),
    () => console.info("*** Price DONE ***"));

portfolio.netWorth(tickers).subscribe(
   data => console.info("Networth =>", data),
   err => console.error("Networth Error => ", err),
   () => console.info('*** Networth DONE ****')
);

// Send to Server
const commands = [
  {
	command: function() {
	  console.info("Sending subscribe..."); 
	  ws.next({ command : "subscribe" });
  	}, 
	time: 3000
  }, 
  {
	command: function() {
	  console.info("Sending unsubscribe...");
	  ws.next({ command : "unsubscribe" });
    },
	time: 8000
  }, 
  {
	command: function() {
	  console.info("Sending complete...");
	  ws.complete(); // closes the connection
    },
	time: 2000
  },
  // {
  //	command: function() {
  // 	  console.info("Sending error...");
  // 	  ws.error({code: 4000, reason: 'App broke'}); //closes the connection
  //    },
  //    time: 2000
  // }
];

rx.from(commands)
  .pipe(concatMap(item => rx.of(item.command).pipe(delay(item.time))))
  .subscribe(cmd => cmd());