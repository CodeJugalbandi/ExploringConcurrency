# Concurrency And Parallelism

Concurrency and Parallelism, these two words have been used quite synonymously and have been easily interchanged with the other.  But many, like Phil Wadler, Simon Peyton Jones and Simon Marlow have given very precise distinctions between these.  In this melody, we will delineate concurrency and parallelism using two problems.

## Problem Statement

* _Concurrency_: Creating an Echo TCP-Server
* _Parallelism_: Splitting a Task (I/O or Computational task)

## CodeJugalbandi

**BRAHMA** Lets tackle Concurrency by looking at this single-threaded Echo TCP-Server in Java.  It waits for connections after creating a ```ServerSocket```.  Once it accepts an incoming socket connection from a client, it goes into an infinite loop, serving that client, by echoing whatever the client sends, except for a ```QUIT``` message - when it moves on to the next waiting client.  

```java
public class Server implements AutoCloseable {
  private final ServerSocket server; 
	
  public Server(String host, int port, int backlogConnectionQueueLength) throws UnknownHostException, IOException {
    server = new ServerSocket(port, backlogConnectionQueueLength, InetAddress.getByName(host));
    System.out.println(Thread.currentThread() + " Created Server");
  }
  
  public void start() {
    System.out.println(Thread.currentThread() + " Server Ready: " + server);
    while (true) {
      acceptAndHandleNewClient(server);
    }
  }

  private void acceptAndHandleNewClient(ServerSocket server) {
    System.out.println(Thread.currentThread() + " Waiting for Incoming connections...");
    try (Socket clientSocket = server.accept()) {
      handleNewClient(clientSocket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void handleNewClient(Socket clientSocket) throws IOException {
    System.out.println(Thread.currentThread() + " Received Connection from " + clientSocket);
    BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    PrintStream os = new PrintStream(clientSocket.getOutputStream());
    // Echo data back to the client, except for QUIT.
    String line = null;
    while ((line = is.readLine()) != null) {
      System.out.println(Thread.currentThread() + " Server Got => " + line);
      if (line.equalsIgnoreCase("QUIT"))
        break;
      else {
        System.out.println(Thread.currentThread() + " Server echoing line back => " + line);
        os.println(line);
        os.flush();
      }
    }
    System.out.println(Thread.currentThread() + " Server Closing Connection by Sending => Ok");
    os.println("Ok");
    os.flush();
    is.close();
    os.close();
  }
	
  public void close() throws IOException {
    server.close();
  }
	
  public static void main(String[] args) {
    try (Server server = new Server("localhost", 8080, 50)) {
      server.start();
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
```
**BRAHMA** Lets start this server.

```shell
$> java Server
Thread[main,5,main] Created Server
Thread[main,5,main] Server Ready: ServerSocket[addr=localhost/127.0.0.1,localport=8080]
Thread[main,5,main] Waiting for Incoming connections...
```

**BRAHMA** Let me use ```telnet``` as one of the clients and send ```HELO```.  It echoes that back.  

```shell 
$> telnet localhost 8080
Trying ::1...
Connection failed: Connection refused
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
HELO
HELO
```

**BRAHMA** Look at the server log, the ```main thread``` is servicing this incoming request:

```shell
Thread[main,5,main] Received Connection from Socket[addr=/127.0.0.1,port=49314,localport=8080]
Thread[main,5,main] Server Got => HELO
Thread[main,5,main] Server echoing line back => HELO
```

**BRAHMA** I can also add more clients that connect to this server and I do this by running 4 clients who try to connect at the same time.

```java
public class Client implements AutoCloseable {
  private final Socket client;
  private final DataOutputStream os;
  private final BufferedReader is;

  public Client(String host, int port) throws UnknownHostException, IOException {
    client = new Socket(host, port);
    os = new DataOutputStream(client.getOutputStream());
    is = new BufferedReader(new InputStreamReader(client.getInputStream()));
  }

  public void sendReceive(String message) {
    try {
      System.out.println("Thread = " + Thread.currentThread());
      System.out.println("Sending to Server: " + message);
      os.writeBytes(message + "\n");
      os.flush();
      // keep on reading from/to the socket till we receive the "Ok" from Server,
      // once we received that we break.
      String responseLine = is.readLine();
      if (responseLine != null) 
        System.out.println("Server Sent: " + responseLine);
		else
		  System.out.println("Server Sent: No Response");
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host: hostname");
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void close() throws IOException {
    sendReceive("QUIT");
    is.close();
    os.close();
  }
  
  public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
    int port = 8080;
    String host = "localhost";
    int totalClients = 4;
    Stream.iterate(1, x -> x + 1).limit(totalClients).forEach(id -> {
      new Thread(() -> {
        try (Client client = new Client(host, port)) {
          client.sendReceive("HELO" + id);
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
    });
  }
}
```

**BRAHMA** As you can see, these are simply waiting in the queue to be served.  

```shell
$> java Client
Thread = Thread[Thread-3,5,main]
Thread = Thread[Thread-0,5,main]
Thread = Thread[Thread-1,5,main]
Thread = Thread[Thread-2,5,main]
Sending to Server: HELO4
Sending to Server: HELO2
Sending to Server: HELO1
Sending to Server: HELO3
```

**BRAHMA** Unless I send ```QUIT``` from the telnet session, they won't be served, because the ```main thread``` is busy serving it and does not even blink for other incoming requests.

```shell
$> telnet localhost 8080
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
HELO
HELO
QUIT
Ok
Connection closed by foreign host.
```

**BRAHMA** Now, the waiting clients are served.

```shell
Thread[main,5,main] Server Got => QUIT
Thread[main,5,main] Server Closing Connection by Sending => Ok
Thread[main,5,main] Waiting for Incoming connections...
Thread[main,5,main] Received Connection from Socket[addr=/127.0.0.1,port=49320,localport=8080]
Thread[main,5,main] Server Got => HELO2
Thread[main,5,main] Server echoing line back => HELO2
Thread[main,5,main] Server Got => QUIT
Thread[main,5,main] Server Closing Connection by Sending => Ok
Thread[main,5,main] Waiting for Incoming connections...
Thread[main,5,main] Received Connection from Socket[addr=/127.0.0.1,port=49319,localport=8080]
Thread[main,5,main] Server Got => HELO3
Thread[main,5,main] Server echoing line back => HELO3
Thread[main,5,main] Server Got => QUIT
Thread[main,5,main] Server Closing Connection by Sending => Ok
...
...
```

**KRISHNA** Oh, of what use is such a server?

**BRAHMA** Exactly, so lets make it concurrent, and the way I do it here is wrap the method call ```handleNewClient(...)``` in a ```CompletableFuture```.  If I were to use the traditional ```Thread``` construct, a new thread needs to be spawned and the server would serve that client on that thread.  But we could do the same using a more modern construct - ```CompletableFuture```

```java
public class Server implements AutoCloseable {
  private final ServerSocket server;
  ...
  ...  
  private void acceptAndHandleClient(ServerSocket server) {
    System.out.println(Thread.currentThread() + " Waiting for Incoming connections...");
    try {
      Socket clientSocket = server.accept();
      CompletableFuture.runAsync(() -> {
        try { 
          handleNewClient(clientSocket);
          clientSocket.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        } 
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  ...
  ...
}
```

**KRISHNA** So, here each connection gets its own thread and they run oblivious of each other when they get scheduled.  This way no new or existing client is blocked because the server is working with all client(s) at the same time, each in its own thread.

**BRAHMA** So, this is Concurrency.  

**MAHESH** Let us look at Parallelism by using an example of splitting an I/O task.  I'll use APL. ```GetPrice``` is a function which does an HTTP GET to retrieve the price for a single stock. In the code below, the central expression is ```(price←Getprice¨codes)```, in which we use the use the "each" operator (```¨```) to map ```GetPrice``` to each element of the array containing stock codes:

```apl

PortfolioSequential←{
  codes←'GOOG' 'AAPL' 'YHOO' 'MSFT' 'ORCL' 'AMZN' 'GOOG'
  quantity←10 20 30 40 40 50 90

  starttime←SessionTime
  price←GetPrice¨codes       ⍝ Get all prices
  networth←price+.×quantity  ⍝ Multiply prices by quantities and sum (vector product)
  ⎕←'Sequential net worth: 'networth('elapsed ms: ',⍕SessionTime-starttime)
}

Sequential net worth:   17194.5  elapsed ms: 8360 
```

**MAHESH** The each operator ```¨``` is a sequential map, so each of
the seven calls to ```GetPrice``` has to complete before the next one starts.  To go parallel, we will one day be able to add a parallel operator ```∥``` and write ```GetPrice∥¨``` codes (read: parallel each). However, at this point in time there is a trial implementation called ```IÏ``` (so named because that looks a bit like ```∥¨```):

```apl
PortfolioParallel←{
  codes←'GOOG' 'AAPL' 'YHOO' 'MSFT' 'ORCL' 'AMZN' 'GOOG'
  quantity←10 20 30 40 40 50 90
     
  starttime←SessionTime
  price←GetPrice IÏ codes   ⍝ IÏ is model of ∥¨ (parallel each)
  networth←price+.×quantity ⍝ Multiply prices by quantities and sum (vector product)
  ⎕←'Parallel net worth: 'networth('elapsed ms: ',⍕SessionTime-starttime)
}

Parallel net worth:   19337.1  elapsed ms: 1234  
```
**MAHESH** The parallel operator invokes the function somewhere in a pool of proceses, and immediately returns a future. price becomes an array of 7 futures, each of which is realized when the corresponding function call completes. On the next line, when price is used in a calculation, APL will automatically block until all values are known, before peforming the vector product.

**BRAHMA** This is indeed interesting to see parallel code rendered in APL. Let me now show the same rendered in Java.  Lets say we have a ```Porfolio``` comprising of several stocks.  In order to calculate the net worth of a portfolio, it uses a proxy ```NationalStockService``` which reaches out over the network to get prices of stocks it holds.  I'll begin with the sequential version first.

```java
public class Portfolio {
  private Map<String, Integer> stocks = new HashMap<>();
  
  public void add(String ticker, Integer qty) {
    int oldQty = 0;
    if (stocks.containsKey(ticker)) {
      oldQty = stocks.get(ticker);
    }
    stocks.put(ticker, oldQty + qty);
  }
	
  public Double netWorth(StockService stockService) throws Exception {
    System.out.println("Stocks = " + stocks);
    long startTime = System.currentTimeMillis();
    List<Double> prices = stocks.entrySet()
      .stream()
      .collect(ArrayList<Double>::new, (acc, entry) -> {
        String ticker = entry.getKey();
        try {
          acc.add(stockService.getPrice(ticker) * entry.getValue());  
        } catch (Exception e) {
          e.printStackTrace();
        }
      }, ArrayList::addAll);
    double worth = prices.stream().reduce(0d, (a, e) -> a + e);
    long timeTaken = System.currentTimeMillis() - startTime;
    System.out.println(String.format("Overall Time %d(ms):", timeTaken));
    return worth;
  }
  
  public static void main(String[] args) throws Exception {
    Portfolio portfolio = new Portfolio();
    portfolio.add("GOOG", 10);
    portfolio.add("AAPL", 20);
    portfolio.add("YHOO", 30);
    portfolio.add("MSFT", 40);
    portfolio.add("ORCL", 40);
    portfolio.add("AMZN", 50);
    portfolio.add("GOOG", 90);
    Thread.sleep(10000);
    double netWorth = portfolio.netWorth(new NationalStockService());
    System.out.println("NetWorth = " + netWorth);
  }
}
```

**BRAHMA** As you can see there is nothing here that runs this code in parallel - all the I/O requests are made on the ```main thread```. For each stock ticker, it gets the prices one after another sequentially and then reduces the prices to net worth.

```shell
> java Portfolio

Stocks = {MSFT=40, GOOG=100, AAPL=20, YHOO=30, ORCL=40, AMZN=50}
Thread[main,5,main] Getting Price for => MSFT
Thread[main,5,main] Returning Price for => MSFT price = 27.37
Thread[main,5,main] Getting Price for => GOOG
Thread[main,5,main] Returning Price for => GOOG price = 102.31
Thread[main,5,main] Getting Price for => AAPL
Thread[main,5,main] Returning Price for => AAPL price = 54.09
Thread[main,5,main] Getting Price for => YHOO
Thread[main,5,main] Returning Price for => YHOO price = 24.31
Thread[main,5,main] Getting Price for => ORCL
Thread[main,5,main] Returning Price for => ORCL price = 70.07
Thread[main,5,main] Getting Price for => AMZN
Thread[main,5,main] Returning Price for => AMZN price = 25.05

Overall Time 3748(ms)
NetWorth = 17192.199999999997
```

**BRAHMA** Java Streams, like the APL ```#.IÏ``` parallel operator have a parallel switch, I'll simply turn on the ```parallel()``` switch on the ```Stream``` and this code now runs in parallel.  Internally, threads are unleashed and each I/O request is now made on a separate thread.

```java { highlight: [9]}
public class Portfolio {
  ...
  ...
  public Double netWorth(StockService stockService) throws Exception {
    ...
    ...
    List<Double> prices = stocks.entrySet()
      .stream()
      .parallel()
      .collect(ArrayList<Double>::new, (acc, entry) -> {
        String ticker = entry.getKey();
        try {
          acc.add(stockService.getPrice(ticker) * entry.getValue());  
        } catch (Exception e) {
          e.printStackTrace();
        }
      }, ArrayList::addAll);
     ...
     ...
  }
  ...
  ...
} 
```

**BRAHMA** The output shows that each I/O request is happening on a separate thread and the overall completion time is 2765 ms, approx 1 sec earlier than the sequential version.

```shell
Stocks = {MSFT=40, GOOG=100, AAPL=20, YHOO=30, ORCL=40, AMZN=50}
Thread[ForkJoinPool.commonPool-worker-9,5,main] Getting Price for => YHOO
Thread[ForkJoinPool.commonPool-worker-11,5,main] Getting Price for => MSFT
Thread[ForkJoinPool.commonPool-worker-2,5,main] Getting Price for => AMZN
Thread[main,5,main] Getting Price for => ORCL
Thread[ForkJoinPool.commonPool-worker-2,5,main] Returning Price for => AMZN price = 28.21
Thread[ForkJoinPool.commonPool-worker-11,5,main] Returning Price for => MSFT price = 21.89
Thread[ForkJoinPool.commonPool-worker-9,5,main] Returning Price for => YHOO price = 29.33
Thread[main,5,main] Returning Price for => ORCL price = 72.22
Thread[ForkJoinPool.commonPool-worker-11,5,main] Getting Price for => GOOG
Thread[ForkJoinPool.commonPool-worker-11,5,main] Returning Price for => GOOG price = 102.45
Thread[ForkJoinPool.commonPool-worker-11,5,main] Getting Price for => AAPL
Thread[ForkJoinPool.commonPool-worker-11,5,main] Returning Price for => AAPL price = 53.6

Overall Time 2765(ms)
NetWorth = 17371.8
```

**BRAHMA** This is Parallel.

**KRISHNA** How is this parallel?  In concurrency also we had threads and in the parallel version also we had threads...so how is this parallel?

**KRISHNA** I would like to change the earlier concurrent server code similar to yours and re-write parts of it.  I will now change the ```start()``` method as:

```java
public class Server implements AutoCloseable {
  private final ServerSocket server;
  ...
  ...
  public void start() {
    System.out.println(Thread.currentThread() + " Server Ready: " + server);
    while (true) {
      Collections.nCopies(4, server)
        .stream()
        .parallel()
        .forEach(this::acceptAndHandleClient);
      }
    }
  }
  ...
  ...
}
```

**KRISHNA** When I run this, I'll be spawning 4 parallel ```accept()```s in 4 different threads.

```shell
Server
Thread[main,5,main] Server Ready: ServerSocket[addr=localhost/127.0.0.1,localport=8080]
Thread[main,5,main] Waiting for Incoming connections...
Thread[ForkJoinPool.commonPool-worker-11,5,main] Waiting for Incoming connections...
Thread[ForkJoinPool.commonPool-worker-2,5,main] Waiting for Incoming connections...
Thread[ForkJoinPool.commonPool-worker-9,5,main] Waiting for Incoming connections...
```

**KRISHNA** And the clients don't have to wait any longer.

```shell
Thread = Thread[Thread-1,5,main]
Thread = Thread[Thread-2,5,main]
Thread = Thread[Thread-0,5,main]
Thread = Thread[Thread-3,5,main]
Sending to Server: HELO2
Sending to Server: HELO1
Sending to Server: HELO3
Sending to Server: HELO4
Server Sent: HELO1
Server Sent: HELO2
Server Sent: HELO4
Server Sent: HELO3
```

**KRISHNA** So would this be parallel or concurrent?

**BRAHMA** Though the form is different, this is still concurrent and not parallel.  Lets reflect on this.

## Reflections

**BRAHMA** In this concurrent server implementation, though we have used the ```parallel()``` switch of the ```Stream``` to accept the connections, it is not an important thing to decide, whether this is parallel or concurrent.  It is still concurrent.  Apart from Server's main thread, there are other threads that can accept new client connections and are not held hostage by a single client, as we still have our ```CompletableFuture``` that handles the client in a separate thread.  So, a new client connecting to the server will never know, how many other clients are currently being served at the same time.  

**KRISHNA** I see what you say... it is important to realize that in the case of earlier concurrent server as well as in this implementation of concurrent server, each of the threads created are serving a particular client oblivious to each other's existence and in no way related to each other.  **They operate independently each serving the client directly**.  Whereas in the splitting I/O task case for getting stock prices, each of the thread was spawned and then the **partial results from each thread was collected to get the total result back as a list of stock prices, which was then served by the main thread to the client**.  So, there is a need for an explicit co-ordinating mechanism that orchestrates this splitting of tasks, scheduling them to collect partial results,  subsequently create a total result and send to the client.  This, I think is the most important factor that delineates Concurrency and Parallelism.

**MAHESH** Like the ```parallel()``` switch, the APL ```∥``` operator, which we also call parallel, is also possibly misnamed: It essentially invokes a function asynchonously and can be used to construct both parallel and concurrent solutions.  It is really the combination of parallel with each (```∥¨```) that simultaneously starts several functions at the same time, that makes it parallel. In some ways, one might even say that it is the resulting USE of the results, in the expression ```networth←price+.×quantity```, where **all the values are used at once**, requiring synchronization, that defines this as a parallel rather than concurrent solution. At the lowest level, we have an ability to launch functions **asynchronously**. Whether the asynchonous call implements concurrency or parallelism depends on the pattern of usage.

**BRAHMA** Yes, indeed! The goal of **Parallelism is Performance** while preserving the functionality of the system, whereas the goal of **Concurrency is Responsiveness**.  These two properties of system are completely orthogonal.  Though both, Concurrency and Parallelism use threads for their implementations, it is important to determine whether these threads are co-ordinated or run independently of each other.  

**KRISHNA** Also, one can mix concurrency and parallelism in the same program and make it responsive and performant at the same time.  Lets move to the next melody.
