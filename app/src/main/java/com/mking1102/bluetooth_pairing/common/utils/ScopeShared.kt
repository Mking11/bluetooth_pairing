package com.mking1102.bluetooth_pairing.common.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class ScopeShared<T>(
    private val childClass: Class<T>,
    private val errorCallback: (Throwable, String) -> Unit,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    val handler = CoroutineExceptionHandler { _, e ->
        errorCallback(e, " ${childClass.canonicalName}  ${childClass.name} : ")
    }

    fun closeRepo() {
        try {
            scope.cancel()
        } catch (e: Exception) {
            errorCallback(e, "${childClass.name} .kt : ")
        }
    }

}