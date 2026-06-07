package com.example

import api.AuthHandler
import api.LoginHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main() {
    val server = HttpServer.create(
        InetSocketAddress(8080),
        0
    )

    server.createContext(
        "/register",
        AuthHandler()
    )

    server.createContext(
        "/login",
        LoginHandler()
    )

    server.executor = null
    server.start()

    println("Server running on port 8080")
}