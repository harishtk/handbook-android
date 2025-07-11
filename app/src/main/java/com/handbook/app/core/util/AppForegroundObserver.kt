package com.handbook.app.core.util

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.handbook.app.common.util.concurrent.ThreadUtil
import java.util.concurrent.CopyOnWriteArraySet

class AppForegroundObserver {

    val listeners: MutableSet<Listener> = CopyOnWriteArraySet()

    @Volatile
    var isForegrounded: Boolean? = null
        private set
        get() {
            return field != null && field == true
        }

    @MainThread
    fun begin() {
        ThreadUtil.assertMainThread()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) { onForeground() }

            override fun onStop(owner: LifecycleOwner) { onBackground() }
        })
    }

    @AnyThread
    fun addListener(listener: Listener) {
        listeners.add(listener)

        if (isForegrounded != null) {
            if (isForegrounded == true) {
                listener.onForeground()
            } else {
                listener.onBackground()
            }
        }
    }

    @AnyThread
    fun removeListener(listener: Listener) { listeners.remove(listener) }

    @MainThread
    fun onForeground() {
        isForegrounded = true

        listeners.onEach(Listener::onForeground)
    }

    @MainThread
    fun onBackground() {
        isForegrounded = false

        listeners.onEach(Listener::onBackground)
    }

    interface Listener {
        fun onForeground() {}
        fun onBackground() {}
    }
}