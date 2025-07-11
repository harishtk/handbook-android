package com.handbook.app.common.util.persistent

import com.handbook.app.common.util.persistent.KeyValueStore

abstract class KvStoreValues(
    private val store: KeyValueStore,
) {

    fun getStore() = store

    abstract fun getKeysToIncludeInBackup(): List<String>

    abstract fun onFirstEverAppLaunch()

    open fun getString(key: String?, defaultValue: String?): String {
        return store.getString(key!!, defaultValue!!)
    }

    open fun getInteger(key: String?, defaultValue: Int): Int {
        return store.getInteger(key!!, defaultValue)
    }

    open fun getLong(key: String?, defaultValue: Long): Long {
        return store.getLong(key!!, defaultValue)
    }

    open fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return store.getBoolean(key!!, defaultValue)
    }

    open fun getFloat(key: String?, defaultValue: Float): Float {
        return store.getFloat(key!!, defaultValue)
    }

    open fun getBlob(key: String?, defaultValue: ByteArray): ByteArray {
        return store.getBlob(key!!, defaultValue)
    }

    open fun putBlob(key: String, value: ByteArray) {
        store.beginWrite().putBlob(key, value).apply()
    }

    open fun putBoolean(key: String, value: Boolean) {
        store.beginWrite().putBoolean(key, value).apply()
    }

    open fun putFloat(key: String, value: Float) {
        store.beginWrite().putFloat(key, value).apply()
    }

    open fun putInteger(key: String, value: Int) {
        store.beginWrite().putInteger(key, value).apply()
    }

    open fun putLong(key: String, value: Long) {
        store.beginWrite().putLong(key, value).apply()
    }

    open fun putString(key: String, value: String) {
        store.beginWrite().putString(key, value).apply()
    }

    open fun remove(key: String) {
        store.beginWrite().remove(key).apply()
    }


}