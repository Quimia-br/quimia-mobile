package com.quimia.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("barcode")
    val barcode: String,
    @SerialName("price")
    val price: Double? = null,
    @SerialName("description")
    val description: String? = null
)
