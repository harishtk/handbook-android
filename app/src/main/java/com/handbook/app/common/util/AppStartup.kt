package com.handbook.app.common.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import com.handbook.app.common.util.concurrent.AppExecutors
import com.handbook.app.tag
import java.util.LinkedList
import kotlin.math.max

class AppStartup {

    val Tag = tag(AppStartup::class.java)

    private val blocking: MutableList<Task>
    private val nonBlocking: MutableList<Task>
    private val postRender: MutableList<Task>

    private val postRenderHandler: Handler

    private var outstandingCriticalRenderEvents: Int = 0

    private var applicationStartTime: Long = 0
    private var renderStartTime: Long = 0
    private var renderEndTime: Long = 0

    init {
        blocking = LinkedList()
        nonBlocking = LinkedList()
        postRender = LinkedList()
        postRenderHandler = Handler(Looper.getMainLooper())
    }

    fun onApplicationCreate() { this.applicationStartTime = System.currentTimeMillis() }

    @MainThread
    fun addBlocking(name: String, task: Runnable): AppStartup {
        blocking.add(Task(name, task))
        return this
    }

    @MainThread
    fun addNonBlocking(task: Runnable): AppStartup {
        nonBlocking.add(Task("", task))
        return this
    }

    @MainThread
    fun addPostRender(task: Runnable): AppStartup {
        postRender.add(Task("", task))
        return this
    }

    @MainThread
    fun onCriticalRenderEventStart() {
        if (outstandingCriticalRenderEvents == 0 && postRender.size > 0) {
            Log.i(Tag, "Received first critical render event.")
            renderStartTime = System.currentTimeMillis()

            postRenderHandler.removeCallbacksAndMessages(null)
            postRenderHandler.postDelayed({
                Log.w(Tag, "Reached the failsafe event for post-render! Either someone forgot to call #onRenderend(), the activity was started while phone was locked, or a app start is taking a very long time.")
                executePostRender()
            }, FAILSAFE_RENDER_TIME)
        }

        outstandingCriticalRenderEvents++
    }

    @MainThread
    fun onCriticalRenderEventEnd() {
        if (outstandingCriticalRenderEvents <= 0) {
            Log.w(Tag, "Too many end events! onCriticalRenderEventStart/End was mismanaged.")
        }

        outstandingCriticalRenderEvents = max(outstandingCriticalRenderEvents - 1, 0)

        if (outstandingCriticalRenderEvents == 0 && postRender.size > 0) {
            renderEndTime = System.currentTimeMillis()

            val coldStartDelta = renderEndTime - applicationStartTime
            Log.i(Tag, "First render has finished. " +
                    "Cold Start: $coldStartDelta ms, " +
                    "Render Time: ${renderEndTime - renderStartTime} ms")
            if (coldStartDelta > COLD_START_TOLERANCE) {
                Log.w(Tag, "Cold start took > $COLD_START_TOLERANCE ms was $coldStartDelta ms")
            }

            postRenderHandler.removeCallbacksAndMessages(null)
            executePostRender()
        }
    }

    @MainThread
    fun execute() {
        blocking.map(Task::runnable).onEach(Runnable::run)
        blocking.clear()

        nonBlocking.map(Task::runnable).onEach(AppExecutors.BOUNDED::execute)
        nonBlocking.clear()

        postRenderHandler.postDelayed(this::executePostRender, UI_WAIT_TIME)
    }

    private fun executePostRender() {
        postRender.map(Task::runnable).onEach(AppExecutors.BOUNDED::execute)
        postRender.clear()
    }

    class Task(
        val name: String,
        val runnable: Runnable
    )

    companion object {
        @Volatile private var INSTANCE: AppStartup? = null

        @Synchronized
        fun getInstance(): AppStartup {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppStartup().also { INSTANCE = it }
            }
        }

        const val UI_WAIT_TIME = 500L
        const val FAILSAFE_RENDER_TIME = 2500L
        const val COLD_START_TOLERANCE = 500L
    }
}