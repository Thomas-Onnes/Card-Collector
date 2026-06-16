package com.example

import api.AuthHandler
import api.LoginHandler
import api.LogoutHandler
import com.sun.net.httpserver.HttpServer
import database.Database
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import api.CollectionsHandler

fun main() {
    val server =
        HttpServer.create(
            InetSocketAddress(8080),
            0
        )

    server.createContext("/register", AuthHandler())
    server.createContext("/login", LoginHandler())
    server.createContext("/logout", LogoutHandler())
    server.createContext("/collections", CollectionsHandler())

    server.executor =
        Executors.newFixedThreadPool(8)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(0)
            Database.close()
        }
    )

    server.start()

    println("Server running on port 8080")
}