package com.handbook.app.feature.onboard.presentation.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.handbook.app.ifDebug
import timber.log.Timber

//@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ShowBiometricPrompt(
    onAuthenticationSucceeded: () -> Unit,
    onAuthenticationError: (String) -> Unit,
    onAuthenticationFailed: () -> Unit,
) {
    val context = LocalContext.current

    val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val manager = BiometricManager.from(context)

    when (manager.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onAuthenticationSucceeded()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        ifDebug { Timber.e("onAuthenticationError: $errorCode, $errString") }
                        onAuthenticationError(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onAuthenticationFailed()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login Required")
                .setSubtitle("Log in using biometric or screen lock")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            onAuthenticationError("No biometric hardware available")

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            onAuthenticationError("Biometric hardware is unavailable")

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            onAuthenticationError("Biometric data is not enrolled")

        else -> {
            onAuthenticationError("Unknown error")
        }
    }

}