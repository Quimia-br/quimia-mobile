package com.quimia.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenericApiResponse<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("error")
    val error: String? = null,
    @SerialName("message")
    val message: String? = null
)
