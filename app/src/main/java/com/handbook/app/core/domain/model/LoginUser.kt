package com.handbook.app.core.domain.model

data class LoginUser(
    val userId: String,
    val username: String,
    val profileName: String,
    val profileImage: String,
    val profileThumb: String,
    val notificationCount: Int,
    val onboardStep: String,
    val role: String,
)

data class ShopData(
    val id: String,
    val name: String,
    val thumbnail: String,
    val category: String,
    val description: String,
    val address: String,
    val image: String,
) {
    companion object {
        fun none(): ShopData {
            return ShopData(
                id = "-1",
                name = "",
                thumbnail = "",
                category = "",
                description = "",
                address = "",
                image = ""
            )
        }
    }
}

