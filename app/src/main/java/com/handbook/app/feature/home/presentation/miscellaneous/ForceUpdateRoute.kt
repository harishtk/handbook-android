package com.handbook.app.feature.home.presentation.miscellaneous

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.handbook.app.R
import com.handbook.app.core.designsystem.component.ThemePreviews
import com.handbook.app.ui.insetLargeMedium
import com.handbook.app.ui.theme.HandbookTheme

@Composable
internal fun ForceUpdateRoute(
    modifier: Modifier = Modifier,
) {

    ForceUpdateScreen(
        modifier = modifier
    )
}

@Composable
internal fun ForceUpdateScreen(
    modifier: Modifier = Modifier,
    onUpdateNowClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            ) {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(
                        R.raw.force_update
                    )
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    isPlaying = true,
                    iterations = LottieConstants.IterateForever,
                    reverseOnRepeat = true
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
            }
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = stringResource(id = R.string.force_update_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.padding(horizontal = insetLargeMedium),
                text = stringResource(id = R.string.force_update_des_1),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier.padding(insetLargeMedium),
                onClick = { onUpdateNowClick() },
            ) {
                Text(text = stringResource(id = R.string.update_now))
            }
        }
    }
}

@ThemePreviews
@Composable
fun ForceUpdateScreenPreview() {
    BoxWithConstraints {
        HandbookTheme {
            ForceUpdateScreen(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}
