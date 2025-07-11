package com.handbook.app.common.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import timber.log.Timber
import java.io.*
import java.util.Locale


object FileUtil {

    fun getFileSize(context: Context, uri: Uri): Long {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")
                .use { pfd -> return pfd!!.statSize }
        } catch (e: FileNotFoundException) {
            Timber.e(e, "File not found: %s", uri.toString())
        } catch (e: IOException) {
            Timber.e(e, "Failed reading file size: %s", uri.toString())
        }
        return 0L
    }

    fun copyFile(
        inputStream: FileInputStream,
        outputStream: FileOutputStream,
        bs: Int = DEFAULT_BUFFER_SIZE
    ): Boolean {
        try {
            BufferedInputStream(inputStream).use { `is` ->
                BufferedOutputStream(outputStream).use { os ->
                    val buff = ByteArray(bs)
                    var read: Int
                    while (`is`.read(buff).also { read = it } != -1) {
                        os.write(buff, 0, read)
                    }
                    os.flush()
                    return true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }

    const val DEFAULT_BUFFER_SIZE = 1024
}