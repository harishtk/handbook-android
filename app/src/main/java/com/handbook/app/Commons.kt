package com.handbook.app

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Size
import android.view.View
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavOptions
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.annotations.Contract
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

typealias Continuation = () -> Unit

private const val EXPORT_DIR = "Export"
private const val TEMP_DIR = "Temp"

private const val FILENAME_REAR = "rearImage.jpg"
private const val FILENAME_FRONT = "frontImage.jpg"
private const val FILENAME_EXPORTED_POST = "exported.jpg"
private const val FILENAME_ACHIEVEMENT_TOKEN = "token.jpg"

public fun parseViews(viewCount: Int): String = when {
    viewCount < 50 -> "$viewCount"
    viewCount < 100 -> "50+"
    viewCount < 1000 -> "${(viewCount / 100).toInt() * 100}+"
    else -> {
        val exp = (ln(viewCount.toDouble()) / ln(1000.0)).toInt()
        val format = DecimalFormat("##.#")
        String.format(
            "${format.format(viewCount / 1000.0.pow(exp.toDouble()))}%c",
            "kMGTPE"[exp - 1]
        )
    }
}

fun defaultNavOptsBuilder(): NavOptions.Builder {
    return NavOptions.Builder()
        .setEnterAnim(R.anim.fragment_open_enter)
        .setExitAnim(R.anim.fragment_open_exit)
        .setPopEnterAnim(R.anim.fragment_close_enter)
        .setPopExitAnim(R.anim.fragment_close_exit)
}

/**
 *
 */
private fun saveImageInCache(
    context: Context,
    outFile: File,
    uri: Uri,
): String {
    val sizeHd = Size(720, 1280)
    val bmp = Glide.with(context)
        .asBitmap()
        .load(uri)
        .override(sizeHd.width, sizeHd.height)
        .submit()
        .get()
    FileOutputStream(outFile).use {
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return outFile.absolutePath
}

fun saveTempImage(
    context: Context,
    bmp: Bitmap,
) : File? {
    runCatching {
        val cacheDir = File(context.cacheDir, TEMP_DIR)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs().also {
                if (!it) {
                    Timber.tag("Export.Msg").w("Cannot create cache dir!")
                }
            }
        }
        val outFile = getNewFile(File(cacheDir, FILENAME_ACHIEVEMENT_TOKEN))
        FileOutputStream(outFile).use {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        outFile
    }.fold(
        onSuccess = { return it },
        onFailure = { return null }
    )
}

private fun galleryAddPic(context: Context, imagePath: String) {
    MediaScannerConnection.scanFile(
        context,
        arrayOf(imagePath),
        null,
        null,
    )
}

fun getRoundedCornerBitmap(bitmap: Bitmap, @Px cornerSize: Float): Bitmap? {
    val output = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, cornerSize, cornerSize, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}

private fun getNewFile(target: File): File {
    if (!target.exists()) target.createNewFile()
    else {
        target.delete()
        target.createNewFile()
    }
    return target
}

fun getRoundedDrawable(@Px radius: Int, bgColor: Int): GradientDrawable {
    return GradientDrawable().apply {
        setColor(bgColor)
        cornerRadii = floatArrayOf(
            radius.toFloat(), radius.toFloat(), // top left
            radius.toFloat(), radius.toFloat(), // top right
            radius.toFloat(), radius.toFloat(), // bottom right
            radius.toFloat(), radius.toFloat()  // bottom left
        )
    }
}

fun makeColoredWeightedSubstring(
    context: Context,
    originalString: String,
    subString: String,
    colorRes: Int = R.color.black,
    onClick: () -> Unit = {},
) : SpannableString {
    val spannableString = SpannableString(originalString)
    try {
        val start = originalString.indexOf(subString)
        val end = start + subString.length
        val color = ResourcesCompat.getColor(
            context.resources,
            colorRes,
            context.theme
        )
        val clickHandler = object : ClickableSpan() {
            override fun onClick(p0: View) {
                onClick()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickHandler, start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(color), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
    } catch (ignore: Exception) { }
    return spannableString
}

fun makeWeightedSubstring(
    context: Context,
    originalString: String,
    subStrings: List<String>,
) : SpannableString {
    val spannableString = SpannableString(originalString)
    try {
        for (subString in subStrings) {
            val start = originalString.indexOf(subString)
            val end = start + subString.length
            val color = ResourcesCompat.getColor(
                context.resources,
                R.color.black,
                null
            )
            spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(color), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        }

    } catch (ignore: Exception) { }
    return spannableString
}

fun makeColoredSubstring(
    context: Context,
    originalString: String,
    subStrings: List<String>,
) : SpannableString {
    val spannableString = SpannableString(originalString)
    try {
        for (subString in subStrings) {
            val start = originalString.indexOf(subString)
            val end = start + subString.length
            val color = ResourcesCompat.getColor(
                context.resources,
                R.color.black,
                null
            )
            spannableString.setSpan(ForegroundColorSpan(color), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    } catch (ignore: Exception) { }
    return spannableString
}

fun makeColoredSubstring(
    originalString: String,
    subStrings: List<String>,
    colors: List<Int>
) : SpannableString {
    val spannableString = SpannableString(originalString)
    try {
        for ((i, subString) in subStrings.withIndex()) {
            val start = originalString.indexOf(subString)
            val color = colors[i]
            val end = start + subString.length
            spannableString.setSpan(ForegroundColorSpan(color), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    } catch (ignore: Exception) { }
    return spannableString
}

fun tag(clazz: Class<*>): String {
    val simpleName = clazz.simpleName
    return simpleName.substring(0, simpleName.length.coerceAtMost(23))
}

fun parseUtmParameters(utmString: String): Map<String, String> {
    // utm_campaign=GTVm5tFUsXvj&utm_medium=invite&utm_source=pepulnow
    try {
        if (utmString.isBlank()) {
            return mapOf()
        }
        val mapping: MutableMap<String, String> = mutableMapOf()
        utmString.split("&").forEach {
            val key = it.split("=")[0]
            val value = it.split("=")[1]
            if (key.isNotBlank()) {
                mapping[key] = value
            }
        }
        return mapping
    } catch (e: IndexOutOfBoundsException) {
        Timber.d(e, "Failed to parse utm parameters")
        return mapOf()
    }
}

fun parseJsonFromString(context: Context, jsonFile: Int): String {
    var resultString: String = ""
    try {
        val raw = context.resources.openRawResource(jsonFile)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        raw.use { rawData ->
            val reader: Reader = BufferedReader(InputStreamReader(rawData, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        }
        resultString = writer.toString()
        return resultString
    } catch (e: Exception) {
        Timber.tag("ERROR").d(e.toString())
        return resultString
    }
}

fun parseQueryParameters(url: String): Map<String, String> {
    // utm_campaign=GTVm5tFUsXvj&utm_medium=invite&utm_source=pepulnow
    try {
        if (url.isBlank()) {
            return mapOf()
        }
        val mapping: MutableMap<String, String> = mutableMapOf()
        url.split("&").forEach {
            val key = it.split("=")[0]
            val value = it.split("=")[1]
            if (key.isNotBlank()) {
                mapping[key] = value
            }
        }
        return mapping
    } catch (e: IndexOutOfBoundsException) {
        Timber.d(e, "Failed to parse query parameters")
        return mapOf()
    }
}

class PhoneNumberFormatException(override val message: String): Exception()

fun isContactPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
}

fun getEmoji(unicode: Int): String {
    return String(Character.toChars(unicode))
}

@OptIn(ExperimentalTime::class)
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

fun getRandomHexCode(): String {
    val random = Random()
    val int = random.nextInt(0xffffff + 1)
    return String.format("#%06x", int)
}

suspend inline fun delayed(millis: Long, block: () -> Unit) {
    delay(millis)
    block()
}

@Contract(value = "null -> null")
fun getFileNameFromUrl(url: String?): String? {
    return getFileNameFromUrl(url, true)
}

@Contract(value = "null, true -> null; null, false -> null")
fun getFileNameFromUrl(url: String?, withExtension: Boolean): String? {
    if (url == null || url.length <= 0) {
        return url
    }
    val lastIndexOfSlash = url.lastIndexOf("/")
    val lastIndexOfDot = url.lastIndexOf(".")
    return try {
        if (withExtension) {
            url.substring(lastIndexOfSlash + 1)
        } else {
            url.substring(lastIndexOfSlash + 1, lastIndexOfDot)
        }
    } catch (e: java.lang.IndexOutOfBoundsException) {
        url
    }
}

fun getInitialCharacter(name: String, limit: Int = 1): String {
    var resultString: String = ""
    val nameWordArray: List<String> = name.split(" ").toMutableList()
    val initialArray: ArrayList<String> = arrayListOf()
    nameWordArray.forEach { word ->
        if (word.isNotBlank()) {
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                .let { initialText ->
                    initialArray.add(initialText.substring(0, 1))
                }
        }
    }
    run loop@{
        initialArray.forEachIndexed { index, value ->
            if (index < limit) {
                resultString += value
            } else {
                return@loop
            }
        }
    }
    return resultString
}

abstract class CountDownRunnable(
    private val start: kotlin.time.Duration,
    private val step: kotlin.time.Duration = 1.seconds,
    private val initialDelay: kotlin.time.Duration = kotlin.time.Duration.ZERO,
) : java.lang.Runnable {
    final override fun run() {
        try {
            var counter: Long = start.toLong(DurationUnit.MILLISECONDS)
            Thread.sleep(initialDelay.toLong(DurationUnit.MILLISECONDS))
            while (counter >= 0) {
                onTick(counter)
                if (counter != 0L) {
                    Thread.sleep(step.toLong(DurationUnit.MILLISECONDS))
                    counter -= step.toLong(DurationUnit.MILLISECONDS)
                } else {
                    break
                }
            }
        } catch (e: InterruptedException) {
            onComplete()
        } finally {
            onComplete()
        }
    }

    abstract fun onTick(millisUntilFinished: Long)
    abstract fun onComplete()
}

fun getGreetingMessage(): String {
    val c = Calendar.getInstance()
    val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

    return when (timeOfDay) {
        in 6..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..20 -> "Good Evening"
        in 21..23 -> "Good Night"
        else -> "Hello"
    }
}