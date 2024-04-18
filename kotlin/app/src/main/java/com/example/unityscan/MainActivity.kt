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

fun broadcastMessage(message: String, address: String, port: Int) {
    try {
        val socket = DatagramSocket()
        socket.broadcast = true

        val buffer = message.toByteArray()
        val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(address), port)
        
        socket.send(packet)
        socket.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun listenToBroadcast(port: Int) {
        try {
            val socket = DatagramSocket(port)
            socket.broadcast = true
            val buffer = ByteArray(1024)

            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)
                println("Received: $message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
}


class MainActivity : ComponentActivity() {
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

        CoroutineScope(Dispatchers.IO).launch {
            listenToBroadcast(41234)
        }


        CoroutineScope(Dispatchers.IO).launch {
            broadcastMessage("Hello, LAN!", "255.255.255.255", 41234)
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