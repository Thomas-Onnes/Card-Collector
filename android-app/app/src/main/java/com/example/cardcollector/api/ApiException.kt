package com.example.cardcollector.api

class ApiException(
    val statusCode: Int,
    message: String
) : Exception(message)
