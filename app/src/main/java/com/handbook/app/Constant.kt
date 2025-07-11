package com.handbook.app

object Constant {
    const val PLATFORM = "android"
    const val PRIVACY_POLICY_URL    = "https://shopsnm.com/terms#privacy"
    const val TERMS_URL             = "https://shopsnm.com/terms"
    const val FAQ_URL = "https://www.shopsnearme.co/#FAQ"
    const val LANDING_URL = "https://www.shopsnearme.co/"
    const val SUPPORT_URL = "https://www.shopsnearme.co/support"

    const val MARKET_URI: String = "market://details?id=${BuildConfig.APPLICATION_ID}"

    const val MIME_TYPE_IMAGE = "image/*"
    const val MIME_TYPE_JPEG = "image/jpeg"
    const val MIME_TYPE_PNG = "image/png"
    const val MIME_TYPE_PLAIN_TEXT = "text/plain"
    const val MIME_TYPE_HLS_STREAM = "application/vnd.apple.mpegurl"
    const val MIME_TYPE_VIDEO = "video/*"
    const val MIME_TYPE_VIDEO_MP4 = "video/mp4"

    /* Env */
    const val ENV_DEV = "dev"
    const val ENV_STAGE = "staging"
    const val ENV_PROD = "prod"
    const val ENV_SPECIAL = "sp"

    /** Permission **/
    const val PERMISSION_DENIED = "PERMISSION_DENIED"
    const val PERMISSION_PERMANENTLY_DENIED = "PERMISSION_PERMANENTLY_DENIED"

    const val USERNAME_PATTERN = "^[a-z0-9]$"
}