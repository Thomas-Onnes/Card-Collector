package com.example

import api.AuthHandler
import api.LoginHandler
import com.sun.net.httpserver.HttpServer
import database.Database
import java.net.InetSocketAddress
import java.util.concurrent.Executors

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    server.createContext("/register", AuthHandler())
    server.createContext("/login", LoginHandler())

    server.executor = Executors.newFixedThreadPool(8)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(0)
            Database.close()
        }
    )

    server.start()

    println("Server running on port 8080")
}