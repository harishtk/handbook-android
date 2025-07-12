package com.handbook.app.feature.home.presentation.party.components.form

import android.os.Parcelable
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize

const val AddressLength = 280

@Parcelize
class AddressState(val initialValue: String) :
    TextFieldState(validator = ::isValidAddress, errorFor = ::descriptionError), Parcelable {
    init {
        text = initialValue
    }
}

private fun isValidAddress(address: String): Boolean {
    return if (address.isNotEmpty()) {
        address.length <= 280
    } else {
        true
    }
}

private fun descriptionError(address: String): String {
    return when {
        address.length > AddressLength -> "Address cannot exceed $AddressLength characters"
        else -> ""
    }
}

val AddressStateSaver = textFieldStateSaver(AddressState(""))