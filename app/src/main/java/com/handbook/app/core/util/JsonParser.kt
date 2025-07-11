package com.handbook.app.core.util

import java.lang.reflect.Type

interface JsonParser {

    fun <T> fromJson(json: String, clazz: Class<T>): T?

    fun <T> toJson(obj: T, type: Type): String?

}