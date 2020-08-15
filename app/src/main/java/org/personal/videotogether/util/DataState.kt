package org.personal.videotogether.util

import java.lang.Exception

sealed class DataState<out R> {
    object Loading: DataState<Nothing>()
    object DuplicatedData: DataState<Nothing>()
    data class Success<out T>(val data: T) : DataState<T>()
    data class NoData(val serverError: String) : DataState<Nothing>()
    data class Error(val exception: Exception) : DataState<Nothing>()
}