package org.personal.videotogether.util.view

import android.util.Log
import androidx.fragment.app.FragmentManager
import org.personal.videotogether.view.dialog.LoadingDialog

class DataStateHandler(private val loadingDialog: LoadingDialog) {

    private val TAG = javaClass.name

    // 로딩 다이얼로그
    fun displayLoadingDialog(isDisplayed: Boolean, childFragmentManager: FragmentManager) {
        if (isDisplayed) {
            if (!loadingDialog.isAdded) {
                loadingDialog.show(childFragmentManager, "LoadingDialog")
            }
        } else {
            loadingDialog.dismiss()
        }
    }

    // 서버통신에 에러가 발생했을 때 로그만 찍어준다 -> product 에서는 사용자에게 보여줄 수 있도록...
    fun displayError(message: String?) {
        if (message == null) {
            Log.i(TAG, "displayError: unknown error")
        } else {
            Log.i(TAG, "displayError: $message")
        }
    }
}