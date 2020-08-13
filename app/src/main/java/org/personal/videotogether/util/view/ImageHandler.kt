package org.personal.videotogether.util.view

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.util.DataState
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ImageHandler {

    private val TAG = javaClass.name

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun imageUriToBitmap(context: Context, contentUri: Uri): Flow<DataState<Bitmap>> = flow {
        emit(DataState.Loading)

        try {
            val contentResolver: ContentResolver = context.contentResolver
            val imageSource = ImageDecoder.createSource(contentResolver, contentUri)
            val bitmap = ImageDecoder.decodeBitmap(imageSource)

            Log.i(TAG, "imageUriToBitmap: $imageSource")
            emit(DataState.Success(bitmap))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "imageUriToBitmap: 이미지 변환 중 에러($e)")
            emit(DataState.Error(e))
        }
    }

    //Bitmap 을 String 형으로 변환
    suspend fun bitmapToString(bitmap: Bitmap?): Flow<DataState<String>> = flow {
        emit(DataState.Loading)

        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val bytes: ByteArray = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

            emit(DataState.Success(base64Image))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bitmapToString: 이미지 변환 중 에러($e)")
            emit(DataState.Error(e))
        }
    }
}