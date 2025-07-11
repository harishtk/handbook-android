package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class IdsRequestDto(
    @SerializedName("ids")
    val ids: List<String>
)
