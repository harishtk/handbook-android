package com.handbook.app.feature.home.presentation.party.components.form

import android.os.Parcelable
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize

const val DescriptionLength = 280

@Parcelize
class DescriptionState(val initialValue: String) :
    TextFieldState(validator = ::isValidDescription, errorFor = ::descriptionError), Parcelable {
    init {
        text = initialValue
    }
}

private fun isValidDescription(description: String): Boolean {
    return if (description.isNotEmpty()) {
        description.length <= 280
    } else {
        true
    }
}

private fun descriptionError(description: String): String {
    return when {
        description.length > DescriptionLength -> "Description cannot exceed $DescriptionLength characters"
        else -> ""
    }
}

val DescriptionStateSaver = textFieldStateSaver(DescriptionState(""))