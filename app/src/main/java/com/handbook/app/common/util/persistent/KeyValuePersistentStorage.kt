package com.handbook.app.common.util.persistent

import com.handbook.app.common.util.persistent.KeyValueDataSet

interface KeyValuePersistentStorage {
    fun writeDateSet(dataSet: KeyValueDataSet, removes: Collection<String>)
    fun getDataSet(): KeyValueDataSet
}