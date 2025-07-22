package com.handbook.app.feature.onboard.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.handbook.app.core.designsystem.component.ThemePreviews
import com.handbook.app.feature.onboard.presentation.util.ShowBiometricPrompt
import com.handbook.app.ui.theme.HandbookTheme

@Composable
internal fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BiometricLoginScreen(
        uiState = uiState,
        onLoginSuccess = viewModel::onLoginSuccess
    )
}

@Composable
private fun BiometricLoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginUiState,
    onLoginSuccess: () -> Unit,
) {
    var showBiometricPrompt by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (uiState) {
                LoginUiState.LoginRequired -> {
                    LoginRequiredView {
                        showBiometricPrompt = true
                    }
                }
                LoginUiState.LoginSuccess -> {
                    LoginSuccessView()
                }
            }
        }

        if (showBiometricPrompt) {
            ShowBiometricPrompt(
                onAuthenticationSucceeded = {
                    onLoginSuccess()
                    showBiometricPrompt = false
                },
                onAuthenticationError = {
                    showBiometricPrompt = false
                },
                onAuthenticationFailed = {
                    showBiometricPrompt = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        showBiometricPrompt = true
    }
}

@Composable
private fun LoginRequiredView(
    modifier: Modifier = Modifier,
    onAuthenticateRequest: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Login Required",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FilledTonalButton(
            onClick = onAuthenticateRequest
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Login",
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(text = "Login", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LoginSuccessView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.padding(32.dp)) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Login Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
                    .size(80.dp)
            )
        }

        Text(text = "Login Successful!", style = MaterialTheme.typography.titleLarge)
    }
}

@ThemePreviews
@Composable
private fun LoginRequiredPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true,
    ) {
        BiometricLoginScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = LoginUiState.LoginRequired,
            onLoginSuccess = {}
        )
    }
}

@ThemePreviews
@Composable
private fun LoginSuccessPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true,
    ) {
        BiometricLoginScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = LoginUiState.LoginSuccess,
            onLoginSuccess = {}
        )
    }
}