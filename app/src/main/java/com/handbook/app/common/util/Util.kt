package com.handbook.app.common.util

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import com.handbook.app.formattedDecimalString
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.text.NumberFormat
import java.util.Currency
import java.util.NavigableMap
import java.util.TreeMap
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Suppress("unused")
object Util {
    const val APPLICATION_COROUTINE_NAME = "AiAvatar::ApplicationCoroutine"

    private const val DEFAULT_COROUTINE_NAME = "AiAvatar::DefaultCoroutine"

    private val defaultCoroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        Timber.e(t)
    }

    fun countDownFlow(
        start: kotlin.time.Duration,
        step: kotlin.time.Duration = 1.seconds,
        initialDelay: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    ): Flow<Long> = flow {
        var counter: Long = start.toLong(DurationUnit.MILLISECONDS)
        delay(initialDelay.toLong(DurationUnit.MILLISECONDS))
        while (counter >= 0) {
            emit(counter)
            if (counter != 0L) {
                delay(step.toLong(DurationUnit.MILLISECONDS))
                counter -= step.toLong(DurationUnit.MILLISECONDS)
            } else {
                break
            }
        }
    }

    fun buildCoroutineScope(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        job: Job = SupervisorJob(),
        exceptionHandler: CoroutineExceptionHandler = defaultCoroutineExceptionHandler,
        coroutineName: String = DEFAULT_COROUTINE_NAME
    ): CoroutineScope {
        val context = dispatcher + job + exceptionHandler
        return CoroutineScope(context = context)
    }

    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    @JvmStatic
    fun isEmpty(value: String?): Boolean {
        return value == null || value.length == 0
    }

    fun hasItems(collection: Collection<*>?): Boolean {
        return collection != null && !collection.isEmpty()
    }

    @JvmStatic
    fun getFirstNonEmpty(vararg values: String?): String? {
        for (value in values) {
            if (!isEmpty(value)) {
                return value
            }
        }
        return ""
    }

    private val suffixes: NavigableMap<Long, String> = TreeMap()
    init {
        suffixes[1_000L] = "k";
        suffixes[1_000_000L] = "M";
        suffixes[1_000_000_000L] = "G";
        suffixes[1_000_000_000_000L] = "T";
        suffixes[1_000_000_000_000_000L] = "P";
        suffixes[1_000_000_000_000_000_000L] = "E";
    }

    fun format(value: Long): String {
        // Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return value.toString() // deal with easy case

        val e: MutableMap.MutableEntry<Long, String> = suffixes.floorEntry(value)!!
        val divideBy: Long = e.key
        val suffix: String = e.value

        val truncated: Long = value / (divideBy / 10) // the number part of the output times 0
        val hasDecimal: Boolean = truncated < 100 && (truncated / 10.0).compareTo(truncated / 10) != 0
        return if (hasDecimal) {
            (truncated / 10.0).formattedDecimalString() + suffix
        } else {
            (truncated / 10).toString() + suffix
        }
    }

    private const val PRE_DOMAIN_PATTERN = "http(?:s)?://"
    fun urlMatches(s1: String, s2: String): Boolean {
        return s1.replace(PRE_DOMAIN_PATTERN.toRegex(), "")
            .equals(s2.replace(PRE_DOMAIN_PATTERN.toRegex(), ""), ignoreCase = true)
    }

    fun formatPrice(price: Long, currencyCode: String): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance(currencyCode)

        return format.format(price)
    }

    fun formatPrice(price: Int, currencyCode: String): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance(currencyCode)

        return format.format(price)
    }

    fun pxToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).roundToInt()
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun dpToPx(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun pxToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun pxToDp(px: Int, context: Context): Int {
        return px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}