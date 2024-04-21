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
import kotlinx.coroutines.*
import com.example.unityscan.connections.ClientManager
import com.example.unityscan.connections.UdpFinder
import com.example.unityscan.connections.HttpServer

class MainActivity : ComponentActivity() {
    private lateinit var udpFinder: UdpFinder
    private lateinit var httpServer: HttpServer
    private var clientManager: ClientManager = ClientManager(5)

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

        // Initialize HttpServer and UdpFinder
        val broadcastPort = 41234 // Example port, replace with your actual port
        val httpPort = 8080 // Example port, replace with your actual port
        val broadcastInterval = 60000L // Example interval in milliseconds

        httpServer = HttpServer(httpPort)
        udpFinder = UdpFinder(baseContext, clientManager, broadcastPort, httpPort, broadcastInterval)
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