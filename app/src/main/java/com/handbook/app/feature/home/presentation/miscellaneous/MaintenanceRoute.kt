package com.handbook.app.feature.home.presentation.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
internal fun MaintenanceRoute(
    modifier: Modifier = Modifier,
) {

    MaintenanceScreen(
        modifier = modifier
    )
}

@Composable
internal fun MaintenanceScreen(
    modifier: Modifier = Modifier,
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
                        R.raw.maintenance
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
            Text(
                text = stringResource(id = R.string.server_maintenance_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier.padding(horizontal = insetLargeMedium),
                text = stringResource(id = R.string.server_maintenance_des_1),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@ThemePreviews
@Composable
fun MaintenanceScreenPreview() {
    BoxWithConstraints {
        HandbookTheme {
            MaintenanceScreen(
                Modifier.background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}
