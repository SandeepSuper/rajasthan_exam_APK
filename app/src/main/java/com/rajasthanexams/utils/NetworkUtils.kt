package com.rajasthanexams.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toFriendlyMessage(): String {
    return when (this) {
        is UnknownHostException, is ConnectException -> "Please check your internet connection and try again."
        is SocketTimeoutException -> "The connection timed out. Please try again."
        else -> this.message ?: "Something went wrong. Please try again later."
    }
}
