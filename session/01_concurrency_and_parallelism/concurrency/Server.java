import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Server implements AutoCloseable {
	private final ServerSocket serverSocket; 
	
	public Server(String host, int port, int backlogConnectionQueueLength) throws UnknownHostException, IOException {
		serverSocket = new ServerSocket(port, backlogConnectionQueueLength, InetAddress.getByName(host));
		System.out.println(Thread.currentThread() + " Created Server");
	}
	
	public void start() {
		System.out.println(Thread.currentThread() + " Server Ready: " + serverSocket);
		while (true) {
      acceptAndHandleClient(serverSocket);
		}
	}

	private void acceptAndHandleClient(ServerSocket serverSocket) {
		System.out.println(Thread.currentThread() + " Waiting for Incoming connections...");
    try {
			Socket clientSocket = serverSocket.accept();
      handleNewClient(clientSocket);
      clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
	}

  private void handleNewClient(Socket clientSocket) throws IOException {
		System.out.println(Thread.currentThread() + " Received Connection from " + clientSocket);
		BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintStream os = new PrintStream(clientSocket.getOutputStream());
		// echo that data back to the client, except for QUIT.
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
		serverSocket.close();
	}
	
	public static void main(String[] args) {
		try (Server serverSocket = new Server("localhost", 8080, 50)) {
			serverSocket.start();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
