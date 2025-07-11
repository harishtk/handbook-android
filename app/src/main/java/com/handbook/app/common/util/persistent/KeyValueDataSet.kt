package com.handbook.app.common.util.persistent

import timber.log.Timber

class KeyValueDataSet : KeyValueReader {
    var values:     HashMap<String, Any>        = hashMapOf()
        private set
    var types:      HashMap<String, Class<*>>   = hashMapOf()
        private set

    fun getType(key: String) = types[key]

    fun put(key: String, value: Any) {
        values[key] = value
        types[key] = Any::class.java
    }

    fun putBlob(key: String, value: ByteArray) {
        values[key] = value
        types[key] = ByteArray::class.java
    }

    fun putBoolean(key: String, value: Boolean) {
        values[key] = value
        types[key] = Boolean::class.java
    }

    fun putFloat(key: String, value: Float) {
        values[key] = value
        types[key] = Float::class.java
    }

    fun putInteger(key: String, value: Int) {
        values[key] = value
        types[key] = Int::class.java
    }

    fun putLong(key: String, value: Long) {
        values[key] = value
        types[key] = Long::class.java
    }

    fun putString(key: String, value: String) {
        values[key] = value
        types[key] = String::class.java
    }

    override fun getBlob(key: String, defaultValue: ByteArray): ByteArray {
        return if (containsKey(key)) {
            readValueAsType(key, ByteArray::class.javaObjectType, false)
        } else {
            defaultValue
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (containsKey(key)) {
            readValueAsType(key, Boolean::class.javaObjectType, false)
        } else {
            defaultValue
        }
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return if (containsKey(key)) {
            readValueAsType(key, Float::class.javaObjectType, false)
        } else {
            defaultValue
        }
    }

    override fun getInteger(key: String, defaultValue: Int): Int {
        return if (containsKey(key)) {
            readValueAsType(key, Int::class.javaObjectType, false)
        } else {
            defaultValue
        }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return if (containsKey(key)) {
            readValueAsType(key, Long::class.javaObjectType, false)
        } else {
            defaultValue
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        return if (containsKey(key)) {
            readValueAsType(key, String::class.javaObjectType, false)
        } else {
            defaultValue
        }
    }

    override fun containsKey(key: String): Boolean {
        return values.containsKey(key)
    }

    fun putAll(other: KeyValueDataSet) {
        values.putAll(other.values)
        types.putAll(other.types)
    }

    fun removeAll(removes: Collection<String>) {
        for (r in removes) { values.remove(r); types.remove(r) }
    }

    private fun <E> readValueAsType(key: String, type: Class<E>, nullable: Boolean): E {
        val value = values[key]
        Timber.d("readValueAsType: val=$value expected=$type got=${value!!::class.java}")
        if (value != null && value::class.java == type) {
            return type.cast(value) as E
        } else if (value == null) {
            throw NullPointerException("value is null")
        } else {
            throw IllegalArgumentException("Type mismatch.")
        }
    }
}