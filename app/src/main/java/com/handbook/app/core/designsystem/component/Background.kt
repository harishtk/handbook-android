package com.handbook.app.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.handbook.app.ui.theme.GradientColors
import com.handbook.app.ui.theme.LocalBackgroundTheme
import com.handbook.app.ui.theme.LocalGradientColors
import com.handbook.app.ui.theme.HandbookTheme
import kotlin.math.tan

/**
 * The main background for the app.
 * Uses [LocalBackgroundTheme] to set the color and tonal elevation of a [Surface].
 *
 * @param modifier Modifier to be applied to the backtorund.
 * @param content The background content.
 */
@Composable
fun HandbookBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val color = LocalBackgroundTheme.current.color
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    Surface(
        color = if (color == Color.Unspecified) Color.Transparent else color,
        tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}

/**
 * A gradient background for select screens. Uses [LocalBackgroundTheme] to set the gradient colors
 * of a [Box] within a [Surface].
 *
 * @param modifier Modifier to be applied to the backround.
 * @param gradientColors The gradient colors to be rendered.
 * @param content The background content.
 */
@Composable
fun HandbookGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    val currentBottomColor by rememberUpdatedState(gradientColors.bottom)
    Surface(
        color = if (gradientColors.container == Color.Unspecified) {
            Color.Transparent
        } else {
            gradientColors.container
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
          Modifier
              .fillMaxSize()
              .drawWithCache {
                  // Compute the start and end coordinates such that the gradients are angled 11.06
                  // degrees off the vertical axis
                  val offset = size.height * tan(
                      Math
                          .toRadians(11.06)
                          .toFloat(),
                  )

                  val start = Offset(size.width / 2 + offset / 2, 0f)
                  val end = Offset(size.width / 2 - offset / 2, size.height)

                  // Create the top gradient that fades out after the halfway point vertically
                  val topGradient = Brush.linearGradient(
                      0f to if (currentTopColor == Color.Unspecified) {
                          Color.Transparent
                      } else {
                          currentTopColor
                      },
                      0.724f to Color.Transparent,
                      start = start,
                      end = end,
                  )

                  // Create the bottom gradient that fades in before the halfway point vertically
                  val bottomGradient = Brush.linearGradient(
                      0.2552f to Color.Transparent,
                      1f to if (currentBottomColor == Color.Unspecified) {
                          Color.Transparent
                      } else {
                          currentBottomColor
                      },
                      start = start,
                      end = end,
                  )

                  onDrawBehind {
                      // There is overlap here, so order is important
                      drawRect(topGradient)
                      drawRect(bottomGradient)
                  }
              }
        ) {
            content()
        }
    }
}

/**
* Multipreview annotation that represents light and dark themes. Add this annotation to a
* composable to render the both themes.
*/
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark theme")
annotation class ThemePreviews

@ThemePreviews
@Composable
fun BackgroundDefault() {
    HandbookTheme(disableDynamicTheming = true) {
        HandbookBackground(Modifier.size(100.dp), content = {})
    }
}

@ThemePreviews
@Composable
fun BackgroundDynamic() {
    HandbookTheme(disableDynamicTheming = false) {
        HandbookBackground(Modifier.size(100.dp), content = {})
    }
}

@ThemePreviews
@Composable
fun BackgroundAndroid() {
    HandbookTheme(androidTheme = true) {
        HandbookBackground(Modifier.size(100.dp), content = {})
    }
}

@ThemePreviews
@Composable
fun GradientBackgroundDefault() {
    HandbookTheme(disableDynamicTheming = true) {
        HandbookGradientBackground(Modifier.size(100.dp), content = {})
    }
}

@ThemePreviews
@Composable
fun GradientBackgroundDynamic() {
    HandbookTheme(disableDynamicTheming = false) {
        HandbookGradientBackground(Modifier.size(100.dp), content = {})
    }
}

@ThemePreviews
@Composable
fun GradientBackgroundAndroid() {
    HandbookTheme(androidTheme = true) {
        HandbookGradientBackground(Modifier.size(100.dp), content = {})
    }
}