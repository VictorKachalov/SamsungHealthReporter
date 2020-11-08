package com.kvs.samsunghealthreporter.observer

import com.kvs.samsunghealthreporter.*
import com.samsung.android.sdk.healthdata.*

class SamsungHealthObserver(
    private val healthDataStore: HealthDataStore
) {
    private val mDataObserver: HealthDataObserver = object : HealthDataObserver(null) {
        override fun onChange(dataTypeName: String) {
            try {
                val type = SamsungHealthType.initWith(dataTypeName)
                mOnNext?.let { it(type) }
            } catch (exception: SamsungHealthTypeException) {
                mOnError?.let { it(exception) }
            }
        }
    }

    private var mOnNext: ((SamsungHealthType) -> Unit)? = null
    private var mOnError: ((Exception) -> Unit)? = null

    fun observe(type: SamsungHealthType): SamsungHealthObserver {
        return this.apply {
            try {
                HealthDataObserver.addObserver(healthDataStore, type.string, mDataObserver)
            } catch (exception: RuntimeException) {
                mOnError?.let { it(exception) }
            }
        }
    }

    fun subscribe(
        onNext: (SamsungHealthType) -> Unit,
        onError: (Exception) -> Unit
    ) {
        this.mOnNext = { onNext(it) }
        this.mOnError = { onError(it) }
    }

    fun dispose() {
        HealthDataObserver.removeObserver(healthDataStore, mDataObserver)
        this.mOnNext = null
        this.mOnError = null
    }
}