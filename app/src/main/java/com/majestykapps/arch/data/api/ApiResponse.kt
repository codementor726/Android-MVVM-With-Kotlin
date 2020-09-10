package com.majestykapps.arch.data.api

import com.google.gson.annotations.SerializedName

/**
 * Sometimes REST APIs will return a JSON payload with the status of the call. This is an example
 * demonstrating how to handle these situations.
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("records") val records: List<T>
)