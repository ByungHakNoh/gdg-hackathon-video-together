package org.personal.videotogether.util

import java.lang.Exception

sealed class DataState<out R> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class NoData(val serverError: String) : DataState<Nothing>()
    object DuplicatedData: DataState<Nothing>()
    data class Error(val exception: Exception) : DataState<Nothing>()
    object Loading: DataState<Nothing>()
}