package com.handbook.app.core.designsystem.component.text

class OtpFieldState(private val initialOtp: String)
    : TextFieldState(validator = ::isValidOtp, errorFor = ::otpErrorMessage) {
        init {
            this.text = initialOtp
        }
}

private fun isValidOtp(otpValue: String): Boolean {
    return otpValue.length == 6
}

private fun otpErrorMessage(otpValue: String): String {
    return when {
        otpValue.isBlank() -> "Enter a otp"
        else -> "OTP contains at least 6 digits"
    }
}

val OtpFieldStateSaver = textFieldStateSaver(OtpFieldState(""))