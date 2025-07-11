package com.handbook.app.feature.home.data.source.remote.model

import com.google.gson.annotations.SerializedName

data class UnreadCountResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("unreadCount")
        val unreadCount: Int
    )
}
