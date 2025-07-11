package com.handbook.app.core.designsystem.component

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.handbook.app.R
import com.handbook.app.ui.defaultCornerSize
import com.handbook.app.ui.insetLargeMedium
import com.handbook.app.ui.theme.DarkGray
import com.handbook.app.ui.theme.HandbookTheme

data class EmptyState(
    val title: String,
    val description: String? = null,
    val showLottie: Boolean = false,
    @RawRes val lottieResId: Int? = null,
    val lottieConfig: LottieConfig? = null,
    val showActionButton: Boolean = false,
    val actionButtonText: String? = null,
    val onActionButtonClick: () -> Unit = {},
) {
    companion object {
        fun noInternet(context: Context): EmptyState {
            return EmptyState(
                title = context.getString(R.string.no_internet_connection),
                description = context.getString(R.string.no_internet_des_1),
                showLottie = true,
                lottieResId = R.raw.globe,
                lottieConfig = LottieConfig(
                    loop = true,
                    autoPlay = true
                )
            )
        }

        fun default(context: Context): EmptyState {
            return EmptyState(
                title = "Nothing found!",
                description = "There is no content right now. Try again later.",
                showLottie = true,
                lottieResId = R.raw.papersad,
                lottieConfig = LottieConfig(
                    loop = true,
                    autoPlay = true
                )
            )
        }
    }
}

data class LottieConfig(
    val loop: Boolean = false,
    val repeatMode: Int = LottieDrawable.RESTART,
    val autoPlay: Boolean = false,
)

@Composable
fun EmptyStateView(
    state: EmptyState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.showLottie) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.33f)
                        .aspectRatio(1f)
                ) {
                    val composition by rememberLottieComposition(
                        spec = LottieCompositionSpec.RawRes(
                            state.lottieResId!!
                        )
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        isPlaying = state.lottieConfig?.autoPlay ?: false,
                        iterations = when (state.lottieConfig?.loop) {
                            true -> LottieConstants.IterateForever
                            else -> 1
                        },
                        reverseOnRepeat = when (state.lottieConfig?.repeatMode) {
                            LottieDrawable.RESTART -> true
                            else -> false
                        }
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                    )
                }
            }
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (!state.description.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = insetLargeMedium),
                    text = state.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGray,
                    textAlign = TextAlign.Center,
                )
            }
            if (state.showActionButton && !state.actionButtonText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { state.onActionButtonClick() },
                    shape = RoundedCornerShape(defaultCornerSize)
                ) {
                    Text(text = state.actionButtonText)
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_3A, showBackground = true, showSystemUi = true)
@Composable
fun EmptyStateViewPreview() {
    BoxWithConstraints {
        HandbookTheme(darkTheme = false) {
            EmptyStateView(
                state = EmptyState(
                    title = "Nothing found!",
                    description = "There no content yet to show here. Come back here after sometime?",
                    showLottie = true,
                    lottieResId = R.raw.papersad,
                    lottieConfig = LottieConfig(loop = true, autoPlay = true, repeatMode = LottieDrawable.RESTART),
                    showActionButton = true,
                    actionButtonText = "Retry",
                ),
                modifier = Modifier.width(400.dp)
            )
        }
    }
}