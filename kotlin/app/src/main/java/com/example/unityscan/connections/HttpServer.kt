package com.example.unityscan.connections

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.receiveText
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*

class HttpServer(private val port: Int) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            start()
        }
    }

    private fun start() {
        embeddedServer(Netty, port = port) {
            routing {
                post("/message") {
                    val receivedText = call.receiveText()
                    println("Received POST request body: $receivedText")

                    call.respondText("Message received", status = HttpStatusCode.OK)
                }
            }
        }.start(wait = true)
    }
}