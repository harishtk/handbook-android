package com.handbook.app.core.domain.model

enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK;

    companion object {
        fun default() = FOLLOW_SYSTEM
    }
}