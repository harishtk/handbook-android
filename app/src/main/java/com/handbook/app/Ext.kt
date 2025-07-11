package com.handbook.app

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.Spanned
import android.util.Size
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.handbook.app.common.util.ServiceUtil
import com.handbook.app.core.Env
import com.handbook.app.core.envForConfig
import timber.log.Timber
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Base64
import java.util.Locale

/**
 * Checks if the receiver is `null` and if so, executes the `nullClause`, forcing an early exit.
 * @param nullClause A block to be performed if receiver is null.
 * This block must end with a `return` statement, forcing an early exit from surrounding scope on `null`.
 * @return The receiver, now guaranteed not to be null.
 */
inline fun<T> T?.guard(nullClause: () -> Nothing): T {
    return this ?: nullClause()
}

fun <T> List<T>.mapButReplace(targetItem: T, newItem: T) = map {
    if (it == targetItem) {
        newItem
    } else {
        it
    }
}

fun <T, R> List<T>.whenNotEmpty(block: () -> R): R? {
    return if (isNotEmpty()) { block() }
    else null
}

fun <T, U> List<T>.intersect(uList: List<U>, filterPredicate: (T, U) -> Boolean) =
    filter { m -> uList.any { filterPredicate(m, it) } }

fun <T> Collection<T>.ifEmpty(block: () -> Unit) {
    if (this.isEmpty()) { block() }
}

fun String?.nullAsEmpty(): String {
    return this ?: ""
}

fun String?.nonNullOrEmpty(block: (s: String) -> Unit) {
    if (this?.isNotEmpty() == true) {
        block(this)
    }
}

fun String.asInitials(limit: Int = 2): String {
    val buffer = StringBuffer()
    trim().split(" ").filter {
        it.isNotEmpty()
    }.joinTo(
        buffer = buffer,
        limit = limit,
        separator = "",
        truncated = "",
    ) { s ->
        s.first().uppercase()
    }
    return buffer.toString()
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.uppercase() }
}

fun String.titleCase(): String {
    return lowercase().replaceFirstChar { it.titlecase() }
}

fun Boolean.toggle(): Boolean {
    return this.not()
}

fun Boolean.toInt(): Int {
    return if (this) { 1 } else { 0 }
}

fun NavController.isOnBackStack(@IdRes id: Int): Boolean =
    try {
        getBackStackEntry(id); true
    } catch (e: Throwable) {
        false
    }


fun EditText.allowOnlyAlphaNumericCharacters() {
    filters = filters.plus(
        InputFilter { src, start, end, dst, dstart, dend ->
            if (src.toString().matches(Regex("[a-zA-Z0-9]+"))) {
                src
            } else ""
        }
    )
}

fun EditText.allowOnlyCapitalNumericCharacters() {
    filters = filters.plus(
        InputFilter { src, start, end, dst, dstart, dend ->
            if (src.toString().matches(Regex("[A-Z0-9]+"))) {
                src
            } else ""
        }
    )
}

fun EditText.hideKeyboard(): Boolean {
    return ServiceUtil.getInputMethodManager(context)
        .hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.showSoftInputMode(): Boolean {
    return ServiceUtil.getInputMethodManager(context)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard(): Boolean {
    return ServiceUtil.getInputMethodManager(context)
        .hideSoftInputFromWindow(windowToken, 0)
}

fun View.showSoftInputMode(): Boolean {
    return ServiceUtil.getInputMethodManager(context)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun Context.showToast(message: String, isShort: Boolean = true) {
    var toastLength: Int = 0
    toastLength = if (isShort) {
        Toast.LENGTH_SHORT
    } else {
        Toast.LENGTH_LONG
    }
    Toast.makeText(this, message, toastLength).show()
}

fun Context.debugToast(message: String, isShort: Boolean = true) {
    if (BuildConfig.DEBUG) {
        showToast(message, isShort)
    }
}


fun Long.format(): String {
    return String.format("%02d", this)
}

fun Double.formattedDecimalString(): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
    formatter.applyPattern("#.##")
    return formatter.format(this)
}

fun Float.format(formatString: String = "0.##"): String {
    val formatter = DecimalFormat(formatString)
    return try {
        formatter.format(this)
    } catch (e: Exception) {
        this.toString()
    }
}

fun Long.pad(): String = format()

object Log {
    @JvmStatic
    fun tag(clazz: Class<*>): String {
        val simpleName = clazz.simpleName
        return if (simpleName.length > 23) {
            simpleName.substring(0, 23)
        } else {
            simpleName
        }
    }
}

inline fun <R> safeCall(block: () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            Timber.e(e)
        }
        null
    }
}

inline fun ifDebug(block: () -> Unit): Boolean {
    if (BuildConfig.DEBUG) {
        block()
        return true
    }
    return false
}

inline fun ifEnvDev(block: () -> Unit): Boolean {
    if (envForConfig(BuildConfig.ENV) == Env.DEV) {
        block()
        return true
    }
    return false
}

/**
 * Calls [block] if [this] is null.
 */
internal inline fun <T> T?.ifNull(block: () -> T): T = this ?: block()

/**
 * Calls [block] if [this] is null. Returns [this] either way.
 */
internal inline fun <T> T?.ifNullAlso(block: () -> Unit): T? = this.also {
    if (it == null) {
        block()
    }
}

fun Boolean.assertBoolean(onTrue: () -> Unit, onFalse: () -> Unit) {
    if (this) onTrue() else onFalse()
}

inline fun Boolean.onElse(block: () -> Unit): Boolean {
    if (!this) {
        block()
        return true
    }
    return false
}

fun Resources.getHtmlSpannedString(@StringRes id: Int): Spanned = getString(id).toHtmlSpan()

fun Resources.getHtmlSpannedString(@StringRes id: Int, vararg formatArgs: Any): Spanned =
    getString(id, *formatArgs).toHtmlSpan()

fun Resources.getQuantityHtmlSpannedString(@PluralsRes id: Int, quantity: Int): Spanned =
    getQuantityString(id, quantity).toHtmlSpan()

fun Resources.getQuantityHtmlSpannedString(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: Any,
): Spanned = getQuantityString(id, quantity, *formatArgs).toHtmlSpan()

fun String.toHtmlSpan(): Spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)

internal val Size.aspectRatio: Float get() {
    return if (width > height) {
        height.toFloat() / width
    } else {
        width.toFloat() / height
    }
}

fun Double.format(formatString: String): String {
    return DecimalFormat(formatString).format(this)
}

fun String.encodeToBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray(StandardCharsets.UTF_8))
}

fun String.decodeFromBase64(): String {
    return Base64.getDecoder().decode(this).toString()
}

inline fun <reified  T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

inline fun <reified  T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}