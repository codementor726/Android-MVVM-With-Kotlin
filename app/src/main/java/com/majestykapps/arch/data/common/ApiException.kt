package com.majestykapps.arch.data.common

class ApiException : Exception {
    constructor(message: String, ex: Exception?) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)
    constructor(t: Throwable) : super(t)
}