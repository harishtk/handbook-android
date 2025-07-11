package com.handbook.app

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.handbook.app.common.util.ByteUnit
import com.handbook.app.common.util.AppStartup
import com.handbook.app.common.util.Inspector
import com.handbook.app.common.util.logging.timber.NoopTree
import com.handbook.app.core.Env
import com.handbook.app.core.di.AppDependencies
import com.handbook.app.core.di.AppDependenciesProvider
import com.handbook.app.core.di.ApplicationCoroutineScope
import com.handbook.app.core.envForConfig
import com.handbook.app.core.net.NetworkSpeedMonitor
import com.handbook.app.core.util.AppForegroundObserver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import javax.inject.Inject
import javax.inject.Provider
import kotlin.system.exitProcess

/**
 * [Application] class for Handbook app
 */
@HiltAndroidApp
class ApplicationContext : Application(),  AppForegroundObserver.Listener, ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    @Inject @ApplicationCoroutineScope
    lateinit var applicationScope: CoroutineScope

    private val networkMonitor: NetworkSpeedMonitor by lazy {
        NetworkSpeedMonitor.getInstance(3_000L)
    }

    override fun onCreate() {
        AppStartup.getInstance().onApplicationCreate()
        val startTime = System.currentTimeMillis()
        super.onCreate()

        val uid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.ApplicationInfoFlags.of(
                PackageManager.GET_META_DATA.toLong())).uid
        } else {
            @Suppress("deprecation")
            packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_META_DATA).uid
        }

        AppStartup.getInstance()
            .addBlocking("init-logging", this::initializeLogging)
            .addBlocking("app-dependencies", this::initApplicationDependencies)
            .addBlocking("lifecycle-observer") {
                AppDependencies.appForegroundObserver?.addListener(this)
            }
            // .addBlocking("after-create", this::setupApp)
            .addNonBlocking(this::checkEmulator)
            .addPostRender(this::setupApp)
            .addPostRender {
                Timber.tag("NetworkSpdMonitor").d("uid=$uid")
                networkMonitor.setAppUid(uid)
            }
            .execute()

        Log.d(
            Tag,
            "onCreate() took " + (System.currentTimeMillis() - startTime) + " ms"
        )

        applicationScope.launch {
            networkMonitor.monitor().collectLatest { speed ->
                val kbps = ByteUnit.BYTES.toKilobytes(speed.bytesPerSecond)
                Timber.tag("NetworkSpdMonitor").d("$kbps KBps")
            }
        }
    }

    private fun setupApp() {
        // AppDependencies.persistentStore?.getOrCreateDeviceId()
        if (envForConfig(BuildConfig.ENV) != Env.PROD) {
            Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException)
        }
    }

    private fun initApplicationDependencies() {
        AppDependencies.init(
            application = this,
            provider = AppDependenciesProvider(this)
        )
        PhoneNumberUtil.init(this)
    }

    private fun initializeLogging() {
        when (envForConfig(BuildConfig.ENV)) {
            Env.DEV -> {
                Timber.plant(Timber.DebugTree())
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    Timber.plant(Timber.DebugTree())
                } else {
                    Timber.plant(NoopTree())
                }
            }
        }
    }

    private fun checkEmulator() {
        if (!BuildConfig.DEBUG) {
            envForConfig(BuildConfig.ENV).let { env ->
                if (env == Env.PROD || env == Env.SPECIAL) {
                    if (Inspector.checkEmulatorFiles()) {
                        Log.e(Tag, "Runtime error", RuntimeException("Invalid runtime. Emulator."))
                        exitProcess(-1)
                    }
                }
            }
        }
    }

    private fun handleUncaughtException(thread: Thread, e: Throwable) {
        Log.e("UncaughtException", "The exception was unhandled", e)
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        e.printStackTrace(printWriter)
        printWriter.close()
        var arr = e.stackTrace
        val report = StringBuilder(
            """$e""".trimIndent()
        )
        report.append("--------- Stack trace ---------\n\n")
        for (stackTraceElement in arr) {
            report.append("    ").append(stackTraceElement.toString()).append("\n")
        }
        report.append("-------------------------------\n\n")
        val cause: Throwable? = e.cause
        if (cause != null) {
            report.append("--------- Cause ---------\n\n")
            report.append(cause.toString()).append("\n\n")
            arr = cause.stackTrace
            for (stackTraceElement in arr) {
                report.append("    ").append(stackTraceElement.toString()).append("\n")
            }
            report.append("-------------------------------\n\n")
        }
        sendEmail(report.toString())
    }

    private fun sendEmail(crash: String) {
        try {
            val reportContent = """
            |DEVICE OS VERSION CODE: ${Build.VERSION.SDK_INT}
            |DEVICE VERSION CODE NAME: ${Build.VERSION.CODENAME}
            |DEVICE NAME: ${Build.MANUFACTURER} ${Build.MODEL}
            |VERSION CODE: ${BuildConfig.VERSION_CODE}
            |VERSION NAME: ${BuildConfig.VERSION_NAME}
            |PACKAGE NAME: ${BuildConfig.APPLICATION_ID}
            |BUILD TYPE: ${BuildConfig.BUILD_TYPE}

            |$crash
            """.trimIndent().trimMargin()
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            emailIntent.data = Uri.parse("mailto:") // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(CRASH_REPORT_EMAIL))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report")
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContent)

            try {
                //start email intent
                val chooser = Intent.createChooser(emailIntent, "Email")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (chooser.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                    System.exit(1)
                } else {
                    throw IllegalStateException("Cannot perform this action!")
                }
            } catch (e: Exception) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                e.printStackTrace()
            }
        } catch (e: Exception) {
            //Timber.tag(TAG).e(TAG, "sendEmail: %s", e.message)
        }
    }

    override fun onForeground() {
        super.onForeground()
        networkMonitor.start()
    }

    override fun onBackground() {
        super.onBackground()
        networkMonitor.stop()
    }

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    companion object {
        const val Tag = "Handbook.Space"

        const val CRASH_REPORT_EMAIL = "support@banterbox.space"

        @Volatile
        var currentVisibleScreen: String = ""
            private set

        @JvmName("setCurrentVisibleScreen1")
        @Synchronized
        fun setCurrentVisibleScreen(tag: String) {
            synchronized(this) {
                currentVisibleScreen = tag
            }
        }

        @Synchronized
        fun clearCurrentVisibleScreen() {
            synchronized(this) {
                currentVisibleScreen = ""
            }
        }
    }
}