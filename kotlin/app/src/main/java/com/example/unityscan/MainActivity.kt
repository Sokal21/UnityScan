package com.example.unityscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.unityscan.ui.theme.UnityScanTheme
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.util.Timer
import java.util.TimerTask

class ClientManager {
    var clientAddresses = mutableListOf<Pair<String, Int>>()

    fun setNewClient(ip: String, port: Int) {
        if (!clientAddresses.any { it.first == ip }) {
            clientAddresses.add(Pair(ip, port))
        }
    }

    fun getAllClients(): List<Pair<String, Int>> {
        return clientAddresses
    }

    fun removeClientByIp(ip: String) {
        clientAddresses = clientAddresses.filter { it.first != ip }.toMutableList()
    }
}


fun broadcastMessage(address: String, port: Int, HTTP_SERVER_PORT: Int) {
    try {
        val socket = DatagramSocket()
        socket.broadcast = true

        val messageJson = JSONObject()
        messageJson.put("port", HTTP_SERVER_PORT)
        
        val message = messageJson.toString()
        val buffer = message.toByteArray()
        val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(address), port)
        
        socket.send(packet)
        socket.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun listenToBroadcast(port: Int, state: ClientManager) {
        try {
            val socket = DatagramSocket(port)
            socket.broadcast = true
            val buffer = ByteArray(1024)

            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)

                if (packet.address.hostAddress != InetAddress.getLocalHost().hostAddress) {
                    val jsonObject = JSONObject(message)
                    val clientPort = jsonObject.getInt("port")
                    state.setNewClient(packet.address.hostAddress, clientPort)
                }

                println("Received: $message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
}

fun periodicallySendMessageToAllClients(state: ClientManager) {
    val timer = Timer()
    val task = object : TimerTask() {
        override fun run() {
            state.clientAddresses.forEach { client ->
                try {
                    val url = URL("http://${client.first}:${client.second}/message")
                    val httpURLConnection = url.openConnection() as HttpURLConnection
                    httpURLConnection.requestMethod = "POST"
                    httpURLConnection.doOutput = true
                    httpURLConnection.setRequestProperty("Content-Type", "application/json")

                    val messageJson = JSONObject()
                    messageJson.put("message", "Hi papu!")
                    val out = BufferedOutputStream(httpURLConnection.outputStream)
                    out.write(messageJson.toString().toByteArray(Charsets.UTF_8))
                    out.flush()
                    out.close()
                    httpURLConnection.connect()

                    val responseCode = httpURLConnection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Handle response
                        val reader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        println("Server Response to ${client.first}: $response")
                    } else {
                        println("Error in sending message to ${client.first}: $responseCode")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    timer.scheduleAtFixedRate(task, 0, 5000)
}




fun startHttpServer(port: Int) {
    val serverSocket = ServerSocket(port)
    println("HTTP Server started on port $port")

    while (true) {
        val clientSocket = serverSocket.accept()
        handleClient(clientSocket)
    }
}

fun handleClient(clientSocket: Socket) {
    val inputStream = clientSocket.getInputStream()
    val reader = BufferedReader(InputStreamReader(inputStream))
    val requestLine = reader.readLine()

    if (requestLine != null && requestLine.startsWith("POST /message ")) {
        val contentLength = getContentLength(reader)
        val body = CharArray(contentLength)
        reader.read(body, 0, contentLength)
        println("Received message: ${String(body)}")
    }

    val outputStream = clientSocket.getOutputStream()
    val response = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n"
    outputStream.write(response.toByteArray())
    outputStream.flush()

    clientSocket.close()
}

fun getContentLength(reader: BufferedReader): Int {
    var contentLength = 0
    while (true) {
        val line = reader.readLine()
        if (line.isNullOrEmpty()) break
        if (line.startsWith("Content-Length: ")) {
            contentLength = line.substringAfter("Content-Length: ").toInt()
        }
    }
    return contentLength
}

class MainActivity : ComponentActivity() {
    private var clientManager: ClientManager = ClientManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnityScanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        var clientManager = this.clientManager

        CoroutineScope(Dispatchers.IO).launch {
            listenToBroadcast(41234, clientManager)
        }
        CoroutineScope(Dispatchers.IO).launch {
            broadcastMessage("255.255.255.255", 41234, 8080)
        }
        CoroutineScope(Dispatchers.IO).launch {
            startHttpServer(8080)
        }
        CoroutineScope(Dispatchers.IO).launch {
            periodicallySendMessageToAllClients(clientManager)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UnityScanTheme {
        Greeting("Android")
    }
}