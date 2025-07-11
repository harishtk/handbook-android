package com.handbook.app.common.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.handbook.app.R

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()
    class QuantityStringResource(
        @param:PluralsRes val resId: Int,
        val quantity: Int,
        vararg val args: Any
    ) : UiText()

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
            is QuantityStringResource -> context.resources.getQuantityString(
                resId, quantity, *args
            )
        }
    }

    internal companion object {
        internal val unknownError: UiText = StringResource(R.string.unknown_error)
        internal val somethingWentWrong: UiText = StringResource(R.string.something_went_wrong_try_later)
        internal val noInternet: UiText = StringResource(R.string.check_your_internet)
        internal val emptyString: UiText = DynamicString("")
    }
}