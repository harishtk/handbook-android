package com.handbook.app.common.util.persistent

import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import com.handbook.app.common.util.concurrent.AppExecutors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

@Suppress("unused")
class KeyValueStore(
    private val storage: KeyValuePersistentStorage
) : KeyValueReader {

    private val executor:       ExecutorService = AppExecutors.newCachedSingleThreadExecutor("pepul-keyValueStore")

    private var dataSet: KeyValueDataSet? = null

    @AnyThread
    @Synchronized
    override fun getBlob(key: String, defaultValue: ByteArray): ByteArray {
        initializeIfNecessary()
        return dataSet!!.getBlob(key, defaultValue)
    }

    @AnyThread
    @Synchronized
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        initializeIfNecessary()
        return dataSet!!.getBoolean(key, defaultValue)
    }

    @AnyThread
    @Synchronized
    override fun getFloat(key: String, defaultValue: Float): Float {
        initializeIfNecessary()
        return dataSet!!.getFloat(key, defaultValue)
    }

    @AnyThread
    @Synchronized
    override fun getInteger(key: String, defaultValue: Int): Int {
        initializeIfNecessary()
        return dataSet!!.getInteger(key, defaultValue)
    }

    @AnyThread
    @Synchronized
    override fun getLong(key: String, defaultValue: Long): Long {
        initializeIfNecessary()
        return dataSet!!.getLong(key, defaultValue)
    }

    @AnyThread
    @Synchronized
    override fun getString(key: String, defaultValue: String): String {
        initializeIfNecessary()
        return dataSet!!.getString(key, defaultValue)
    }

    @AnyThread
    @Synchronized
    override fun containsKey(key: String): Boolean {
        initializeIfNecessary()
        return dataSet!!.containsKey(key)
    }

    private fun initializeIfNecessary() {
        if (dataSet != null) return
        this.dataSet = storage.getDataSet()
    }

    @AnyThread
    fun beginWrite() = Writer()

    @AnyThread
    @Synchronized
    fun beginRead(): KeyValueReader {
        initializeIfNecessary()

        val copy = KeyValueDataSet()
        copy.putAll(dataSet!!)

        return copy
    }

    @AnyThread
    @Synchronized
    fun blockUntilAllWritesFinished() {
        val latch = CountDownLatch(1)

        /* Single threaded executor waits in queue for the
        * write operation to complete */
        executor.execute(latch::countDown)

        try {
            latch.await()
        } catch (e: InterruptedException) {
            Log.w(TAG, "Failed to wait for all writes")
        }
    }

    @Synchronized
    fun write(newDataSet: KeyValueDataSet, removes: Collection<String>) {
        initializeIfNecessary()

        dataSet!!.putAll(newDataSet)
        dataSet!!.removeAll(removes)

        executor.execute { storage.writeDateSet(newDataSet, removes) }
    }

    /**
     * Forces the store to re-fetch all of it's data from the database.
     */
    @Synchronized
    fun resetCache() {
        dataSet = null
        initializeIfNecessary()
    }

    inner class Writer {
        private val dataSet: KeyValueDataSet = KeyValueDataSet()
        private val removes:    MutableSet<String>      = HashSet()

        fun putBlob(key: String, value: ByteArray): Writer {
            dataSet.putBlob(key, value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): Writer {
            dataSet.putBoolean(key, value)
            return this
        }

        fun putFloat(key: String, value: Float): Writer {
            dataSet.putFloat(key, value)
            return this
        }

        fun putInteger(key: String, value: Int): Writer {
            dataSet.putInteger(key, value)
            return this
        }

        fun putLong(key: String, value: Long): Writer {
            dataSet.putLong(key, value)
            return this
        }

        fun putString(key: String, value: String): Writer {
            dataSet.putString(key, value)
            return this
        }

        fun remove(key: String): Writer {
            removes.add(key)
            return this
        }

        @AnyThread
        fun apply() {
            for (key in removes) {
                if (dataSet.containsKey(key)) {
                    throw IllegalStateException("Tried to remove a key while also setting it!")
                }
            }

            write(dataSet, removes)
        }

        @WorkerThread
        fun commit() {
            apply()
            blockUntilAllWritesFinished()
        }
    }

    companion object {
        private val TAG = KeyValueStore::class.java.simpleName
    }
}