package com.handbook.app.core.domain.model

import com.handbook.app.core.data.source.local.entity.NotificationEntity
import com.handbook.app.toInt

data class HandbookNotification(
    val content: String,
    val timestamp: String,
    val read: Boolean,
    val category: String,
    val subCategory: String,
    val userId: String,
) {
    var id:         Long?   = null
    var title:      String? = null
    var shopId:     String? = null
    var productId:  String? = null
}

fun HandbookNotification.asEntity(): NotificationEntity {
    return NotificationEntity(
        content = content,
        timestamp = timestamp,
        read = read.toInt(),
        category = category,
        subCategory = subCategory,
        userId = userId
    ).also {
        it._id = id
        it.title = title
    }
}

data class AiaTransaction(
    val transactionId: String,
    val price: Int,
    val photo: Int,
    val style: Int,
    val variation: Int,
    val currencySymbol: String,
    val currencyCode: String,
    val timestamp: String,
    val productName: String,
) {
    var id:         Long?   = null
}

data class AiaCoupon(
    val id: Long,
    val couponCode: String,
    val variation: Int,
    val photo: Int,
    val style: Int,
)