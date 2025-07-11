package com.handbook.app.core.designsystem.component.text

class CountryCodeState(val isoCode: String) :
    TextFieldState(validator = ::isCountryCodeValid) {

    init {
        text = this.isoCode
    }
}

private fun isCountryCodeValid(isoCode: String): Boolean {
    return isoCode.isNotBlank()
}