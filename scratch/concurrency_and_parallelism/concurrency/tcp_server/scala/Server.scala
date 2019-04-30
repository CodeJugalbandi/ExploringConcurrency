package com.tsys

import java.io._
import java.net.{InetAddress, ServerSocket, Socket, UnknownHostException}
import scala.util.{Try, Success, Failure}

object Server {
  def startServer(host: String, port: Int, backlogConnectionQueueLength: Int = 50)(handler: (InputStream, OutputStream) => Unit) = Try {
    using(new ServerSocket(port, backlogConnectionQueueLength, InetAddress.getByName(host))) { server => 
    	println(s"${Thread.currentThread}: $server Ready")
  	  while (true) {
      	println(s"${Thread.currentThread}: $server Waiting for Incoming connections...")
        using(server.accept()) { clientSocket => 
      		println(s"${Thread.currentThread}: Received Connection from $clientSocket")
          using(clientSocket.getInputStream()) { is =>
            using(clientSocket.getOutputStream()) { os =>
              handler(is, os)
            }
          }
        }
  	  }
    }
  }

  def echoHandler(is: InputStream, os: OutputStream): Unit = {
    println(s"${Thread.currentThread}: <= Handler Thread")
    using(new BufferedReader(new InputStreamReader(is))) { br =>
      using(new PrintWriter(os, true)) { pw =>
        import util.control.Breaks._
        breakable {
        	var line: String = null
      	  while (!(line = br.readLine()).equals(null)) {
            println(s"${Thread.currentThread}: Server Got => $line")
            // echo that line back to the client, except for QUIT.
            if (line.equalsIgnoreCase("QUIT")) break
            else {
              println(s"${Thread.currentThread}: Server echoing line back => $line")
        			pw.println(line)
            }
          }
      	}
        println(s"${Thread.currentThread}: Server Closing Connection by Sending => Ok")
      	pw.println("Ok")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    // println(startServer("local", 8080) { case (is, os) =>
    println(startServer("localhost", 8080) { case (is, os) => 
      echoHandler(is, os) 
    })
  }
}
