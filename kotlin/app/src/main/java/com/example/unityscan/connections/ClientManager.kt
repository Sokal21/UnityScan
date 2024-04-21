package com.example.unityscan.connections

import java.util.*

class ClientManager(private val minutesToDisconnect: Long) {
    private var clientAddresses = mutableMapOf<String, Pair<Int, Date>>()

    init {
        periodicallyCheckClients()
    }

    fun setNewClient(ip: String, port: Int) {
        clientAddresses[ip] = Pair(port, Date())
    }

    fun getAllClients(): List<Triple<String, Int, Date>> {
        return clientAddresses.map { Triple(it.key, it.value.first, it.value.second) }
    }

    fun removeClientByIp(ip: String) {
        clientAddresses.remove(ip)
    }

    private fun periodicallyCheckClients() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val currentTime = Date()
                clientAddresses.entries.removeIf {
                    val diff = currentTime.time - it.value.second.time
                    val diffMinutes = diff / (60 * 1000) % 60
                    diffMinutes > minutesToDisconnect
                }
            }
        }, 0, 60000) // Check every minute
    }
}