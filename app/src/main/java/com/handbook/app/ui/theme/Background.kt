package com.handbook.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import javax.annotation.concurrent.Immutable

/**
 * A class to model background color and tonal elevation values for Handbook Seller.
 */
@Immutable
data class BackgroundTheme(
    val color: Color = Color.Unspecified,
    val tonalElevation: Dp = Dp.Unspecified,
)

/**
 * A composition local for [BackgroundTheme]
 */
val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }