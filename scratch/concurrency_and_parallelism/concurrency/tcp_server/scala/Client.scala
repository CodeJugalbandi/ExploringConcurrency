package com.tsys

import java.io._
import java.net._
import scala.util.{Try, Success, Failure}

class Client(host: String, port: Int) extends AutoCloseable {
	val client = new Socket(host, port)
	val os = new DataOutputStream(client.getOutputStream())
	val is = new BufferedReader(new InputStreamReader(client.getInputStream()))

	def sendReceive(message: String): Try[Unit] = Try {
		println(s"${Thread.currentThread}: Sending to Server => $message")
		os.writeBytes(message + "\n")
		os.flush()
		// keep on reading from/to the socket till we receive the "Ok" from Server,
		// once we received that we break.
		val responseLine = is.readLine()
	  if (!responseLine.equals(null)) println("Server Sent: " + responseLine)
		else println("Server Sent: No Response")
	}

	override def close(): Unit = {
		sendReceive("QUIT")
		is.close()
		os.close()
	}
}

object Client {
  def main(args: Array[String]): Unit = {
    val totalClients = 4
    Stream.from(1, 1).take(totalClients).foreach(id => {
    	new Thread(() => {
    		using(new Client("localhost", 8080)) { client =>
    			client.sendReceive(s"HELO$id")
    			Thread.sleep(2000)
        }
    	}).start
    })
  }
}