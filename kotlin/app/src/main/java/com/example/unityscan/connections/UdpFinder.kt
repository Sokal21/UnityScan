package com.example.unityscan.connections

import android.annotation.SuppressLint
import android.content.Context
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.coroutines.*
import android.provider.Settings

class UdpFinder(
    private val context: Context, // Add context as a parameter
    private val clients: ClientManager,
    private val broadcastPort: Int,
    private val httpPort: Int,
    private val broadcastInterval: Long
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            listenToBroadcast()
        }
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) { // Keep sending messages periodically as long as the coroutine is active
                sendBroadcastMessage(hostInformationPayload().toString())
                delay(broadcastInterval) // Wait for the specified interval before sending the next message
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun hostInformationPayload(): JSONObject {
        val json = JSONObject()
        json.put("port", this.httpPort)
        json.put("device_id", getDeviceId())
        return json
    }
    
    fun sendBroadcastMessage(message: String) {
        try {
            val socket = DatagramSocket()
            socket.broadcast = true
    
            val buffer = message.toByteArray()
            val packet = DatagramPacket(
                buffer,
                buffer.size,
                InetAddress.getByName("255.255.255.255"),
                this.broadcastPort
            )
            
            socket.send(packet)
            socket.close()
    
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun listenToBroadcast() {
        try {
            val socket = DatagramSocket(this.broadcastPort)
            socket.broadcast = true
            val buffer = ByteArray(1024)

            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)

                val jsonObject = JSONObject(message)
                val clientPort = jsonObject.getInt("port")
                val deviceId = jsonObject.getString("device_id")

                println("Device ID: $deviceId")
                println("Current Device ID: ${getDeviceId()}")
                
                if (deviceId != getDeviceId()) {
                    packet.address.hostAddress?.let { this.clients.setNewClient(it, clientPort) }
                }

                println("Received: $message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}