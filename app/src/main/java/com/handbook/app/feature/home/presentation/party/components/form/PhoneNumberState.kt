package com.handbook.app.feature.home.presentation.party.components.form

import android.os.Parcelable
import android.util.Patterns
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize
import java.util.regex.Pattern

const val PhoneNumberLength = 10

@Parcelize
class PhoneNumberState(
    val phone: String? = null
) : TextFieldState(errorFor = ::phoneValidationError), Parcelable {

    override val isValid: Boolean
        get() = isPhoneValid(phone = text, isoCode = "IN")

    init {
        phone?.let { text = it }
    }

    companion object {
        fun isValidPhone(phone: String?, isoCode: String): Boolean {
            // Phone validation
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            return try {
                val phoneNumberLength = phoneNumberUtil.parse(phone, isoCode)
                phoneNumberUtil.isValidNumber(phoneNumberLength)
            } catch (e: Exception) {
                false
            }
        }
    }
}

private fun isPhoneValid(phone: String?, isoCode: String): Boolean {
    return !phone.isNullOrBlank() && isValidFormat(phone) && PhoneNumberState.isValidPhone(phone, isoCode)
}

private fun isValidFormat(phone: String): Boolean {
    return Pattern.matches(Patterns.PHONE.toString(), phone)
}

private fun phoneValidationError(phone: String): String {
    return "Please check the number"
}

val PhoneNumberStateSaver = textFieldStateSaver(PhoneNumberState())