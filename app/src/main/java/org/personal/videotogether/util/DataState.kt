package org.personal.videotogether.util

import java.lang.Exception

sealed class DataState<out R> {
    object Loading: DataState<Nothing>()
    object DuplicatedData: DataState<Nothing>()
    object NoData : DataState<Nothing>()
    data class Success<out T>(val data: T) : DataState<T>()
    data class GetState<out T>(val state:T): DataState<T>()
    data class Error(val exception: Exception) : DataState<Nothing>()
}