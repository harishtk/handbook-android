package com.handbook.app.core.designsystem

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.handbook.app.BuildConfig
import com.handbook.app.Log
import timber.log.Timber
import kotlin.random.Random

private val randomColor
    get() = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

/**
 * A [Modifier] that draws the Rectangular UI Bounds around the element.
 */
@Stable
fun Modifier.exposeBounds(): Modifier = this.then(exposeBoundsModifier)

private val exposeBoundsModifier =
    Modifier.drawWithCache {
        // Sanity check
        if (BuildConfig.DEBUG) {
            val message = "exposeBounds modifier is expected to use in only debug mode."
            val t = IllegalStateException(message)
            Timber.tag("exposeBounds").w(t)
        }

        onDrawWithContent {
            // Draw actual content.
            drawContent()

            val color = randomColor
            val strokeWidthPx = 1f

            val halfStroke = strokeWidthPx / 2
            val topLeft = Offset(halfStroke, halfStroke)
            val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

            val fillArea = (strokeWidthPx * 2) > size.minDimension
            val rectTopLeft = if (fillArea) Offset.Zero else topLeft
            val size = if (fillArea) size else borderSize
            val style = if (fillArea) Fill else Stroke(strokeWidthPx)

            drawRect(
                brush = SolidColor(color),
                topLeft = rectTopLeft,
                size = size,
                style = style
            )
        }
    }