package com.handbook.app.core.domain.model

enum class ThemeBrand {
    DEFAULT, ANDROID;

    companion object {
        fun default() = ANDROID
    }
}