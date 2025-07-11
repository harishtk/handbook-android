package com.handbook.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import com.handbook.app.feature.home.domain.model.HandbookNotification
import com.handbook.app.feature.home.domain.model.HandbookNotificationType

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("subType") val subType: String?,
    @SerializedName("message") val message: String,
    @SerializedName("referenceId") val referenceId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("read") val read: Boolean,
    @SerializedName("recipientId") val recipientId: String,
    @SerializedName("actorId") val actorId: String,
    @SerializedName("actor") val actor: UserSummaryDto
)

fun NotificationDto.toHandbookNotification(): HandbookNotification {
    return HandbookNotification(
        id = id,
        type = HandbookNotificationType.fromString(type),
        subType = subType,
        message = message ?: "",
        referenceId = referenceId,
        createdAt = createdAt,
        read = read,
        recipientId = recipientId,
        actorId = actorId,
        actor = actor.toUserSummary(),
    )
}