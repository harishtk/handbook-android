package com.handbook.app.core.designsystem

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize

/**
 * A composition local for [WindowSizeClass]
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSizeClass: ProvidableCompositionLocal<WindowSizeClass> = staticCompositionLocalOf {
    WindowSizeClass.calculateFromSize(DpSize.Zero)
}