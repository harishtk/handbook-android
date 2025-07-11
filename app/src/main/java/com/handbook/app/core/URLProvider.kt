package com.handbook.app.core

import com.handbook.app.BuildConfig
import org.jetbrains.annotations.Contract

private const val THUMBNAIL_BASE_URL    = "https://d1whtbopipnjq0.cloudfront.net"
private const val VIDEO_BASE_URL        = "https://d1whtbopipnjq0.cloudfront.net"

object URLProvider {

    private const val baseUrl: String = BuildConfig.BASE_URL
    private const val videoBaseUrl: String = "<Enter you URL here>"
    private val environment: Env = envForConfig(BuildConfig.ENV)

    fun getImageUrl(
        source: URLSource,
        fileName: String,
        sourceCategory: SourceCategory = SourceCategory.ORIGINAL,
    ): String {
        val prefix = buildPrefix(source, sourceCategory)
        return "$prefix$fileName"
    }

    @Contract("null -> null")
    fun thumbnailUrl(fileName: String?): String? {
        fileName ?: return null
            return getImageUrl(URLSource.VIDEO, fileName, SourceCategory.THUMBNAIL)
    }

    @Contract("null -> null")
    fun bunnyThumbnail(videoId: String?): String? {
        videoId ?: return null
            return StringBuilder(videoBaseUrl)
                .append(videoId).append("/").append("thumbnail.jpg").toString()
    }

    @Contract("null -> null")
    fun profileUrl(fileName: String?): String? {
        fileName ?: return null
        return getImageUrl(URLSource.PROFILE, fileName, SourceCategory.ORIGINAL)
    }

    @Contract("null -> null")
    fun profileThumbUrl(fileName: String?): String? {
        fileName ?: return null
        return getImageUrl(URLSource.PROFILE, fileName, SourceCategory.THUMBNAIL)
    }

    @Contract("null -> null")
    fun videoUrl(fileName: String?): String? {
        fileName ?: return null
            return getImageUrl(URLSource.VIDEO, fileName)
    }

    @Contract("null -> null")
    fun avatarThumbUrl(fileName: String?): String? {
        fileName ?: return null
            return getImageUrl(URLSource.VIDEO, fileName, SourceCategory.THUMBNAIL)
    }

    private fun buildPrefix(source: URLSource, sourceCategory: SourceCategory): String {
        return when (environment) {
            Env.DEV -> when (source) {
                URLSource.VIDEO -> {
                    when (sourceCategory) {
                        SourceCategory.THUMBNAIL -> "${baseUrl}thumbnail/"
                        SourceCategory.ORIGINAL -> "$videoBaseUrl"
                    }
                }
                URLSource.PROFILE -> {
                    when (sourceCategory) {
                        SourceCategory.THUMBNAIL -> "${baseUrl}thumbnail/"
                        SourceCategory.ORIGINAL -> "${baseUrl}image/"
                    }
                }
            }
            Env.PROD, Env.INTERNAL -> when (source) {
                URLSource.VIDEO -> {
                    when (sourceCategory) {
                        SourceCategory.THUMBNAIL -> "${baseUrl}thumbnail/"
                        SourceCategory.ORIGINAL -> "$videoBaseUrl"
                    }
                }
                URLSource.PROFILE -> {
                    when (sourceCategory) {
                        SourceCategory.THUMBNAIL -> "${baseUrl}thumbnail/"
                        SourceCategory.ORIGINAL -> "${baseUrl}image/"
                    }
                }
            }
            Env.STAGE,
            Env.SPECIAL, -> {
                ""
            }
        }
    }
}

enum class URLSource {
    VIDEO, PROFILE
}

enum class SourceCategory {
    THUMBNAIL, ORIGINAL
}
