package com.handbook.app.core.designsystem.component.text

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SimpleTextFieldState(val initialValue: String = "")
    : TextFieldState(), Parcelable {
        init {
            text = initialValue
        }
}