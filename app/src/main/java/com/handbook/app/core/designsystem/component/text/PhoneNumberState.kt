package com.handbook.app.core.designsystem.component.text

import android.util.Patterns
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.handbook.app.nullAsEmpty
import java.util.regex.Pattern

class PhoneNumberState(
    private val countryCodeState: CountryCodeState? = null,
    val phone: String? = null
) : TextFieldState(errorFor = ::phoneValidationError) {

    override val isValid: Boolean
        get() = isPhoneValid(text, countryCodeState?.text.nullAsEmpty())

    init {
        phone?.let { text = it }
    }

    companion object {
        fun isValidPhone(phone: String?, isoCode: String?): Boolean {
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
    return "Please check your number"
}

val PhoneNumberStateSaver = textFieldStateSaver(PhoneNumberState())