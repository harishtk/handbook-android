package com.handbook.app.feature.home.presentation.party.addparty

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.hypot

fun Modifier.circularRevealScale(
    fabCenterInRoot: Offset,
    containerOffsetInRoot: Offset,
    fabRadiusPx: Float,
    animationProgress: Float,
    containerSize: IntSize
): Modifier = this.then(
    Modifier.graphicsLayer {
        if (
            fabCenterInRoot == Offset.Zero ||
            containerOffsetInRoot == Offset.Zero ||
            containerSize == IntSize.Zero
        ) {
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
            return@graphicsLayer
        }

        val fabCenterInLocal = fabCenterInRoot - containerOffsetInRoot
        val maxRadius = hypot(containerSize.width.toDouble(), containerSize.height.toDouble()).toFloat()
        val currentRadius = lerp(fabRadiusPx, maxRadius, animationProgress)
        val scale = currentRadius / fabRadiusPx

        scaleX = scale
        scaleY = scale
        alpha = animationProgress

        val originX = fabCenterInLocal.x / containerSize.width
        val originY = fabCenterInLocal.y / containerSize.height
        transformOrigin = TransformOrigin(originX, originY)

        clip = true
        shape = CircleShape
    }
)


@Composable
fun CircularRevealBox(
    fabCenterInRoot: Offset,
    fabRadiusPx: Float,
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var containerOffsetInRoot by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val animationProgress by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    Box(
        modifier = modifier
            .onGloballyPositioned {
                containerOffsetInRoot = it.localToRoot(Offset.Zero)
                containerSize = it.size
            }
            .drawWithContent {
                if (fabCenterInRoot == Offset.Zero || containerSize == IntSize.Zero) {
                    drawContent()
                    return@drawWithContent
                }

                val fabCenterInLocal = fabCenterInRoot - containerOffsetInRoot

                val maxRadius = hypot(size.width.toDouble(), size.height.toDouble()).toFloat()
                val currentRadius = lerp(fabRadiusPx, maxRadius, animationProgress)

                val path = Path().apply {
                    addOval(Rect(center = fabCenterInLocal, radius = currentRadius))
                }

                clipPath(path, clipOp = ClipOp.Intersect) {
                    this@drawWithContent.drawContent()
                }
            }
            .background(MaterialTheme.colorScheme.primary)
    ) {
        content()
    }
}

@Composable
fun CircularRevealFromFabScreen() {
    var isRevealed by remember { mutableStateOf(false) }
    var fabCenter by remember { mutableStateOf(Offset.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isRevealed = !isRevealed },
                modifier = Modifier.onGloballyPositioned { coords ->
                    val localOffset = coords.localToRoot(Offset.Zero)
                    fabSize = coords.size
                    fabCenter = localOffset + Offset(
                        x = coords.size.width / 2f,
                        y = coords.size.height / 2f
                    )
                }
                    .systemBarsPadding()
                    .imePadding()
            ) {
                Icon(
                    imageVector = if (isRevealed) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Base
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Base Content Layer")
            }

            // Circular Reveal Layer
            CircularRevealBox(
                fabCenterInRoot = fabCenter,
                fabRadiusPx = fabSize.width / 2f,
                isRevealed = isRevealed,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Revealed Content!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReveal() {
    MaterialTheme {
        CircularRevealFromFabScreen()
    }
}
