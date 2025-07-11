package com.handbook.app.feature.home.domain.model

data class HandbookNotification(
    val id: String,
    val type: HandbookNotificationType,
    val subType: String?,
    val message: String,
    val referenceId: String?,
    val createdAt: String,
    val read: Boolean,
    val recipientId: String,
    val actorId: String?,
    val actor: UserSummary,
)

enum class HandbookNotificationType {
    FOLLOW, LIKE, GENERAL, ANNOUNCEMENTS, UNKNOWN;

    companion object {
        fun fromString(type: String): HandbookNotificationType {
            return when (type) {
                "FOLLOW" -> FOLLOW
                "LIKE" -> LIKE
                "GENERAL" -> GENERAL
                "ANNOUNCEMENTS" -> ANNOUNCEMENTS
                else -> UNKNOWN
            }
        }
    }
}