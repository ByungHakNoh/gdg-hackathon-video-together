package org.personal.videotogether.util

import androidx.fragment.app.FragmentManager
import org.personal.videotogether.view.dialog.LoadingDialog

class DataStateHandler(private val loadingDialog: LoadingDialog) {

    // 로딩 다이얼로그
    fun displayLoadingDialog(isDisplayed: Boolean, childFragmentManager: FragmentManager) {
        if (isDisplayed) {
            loadingDialog.show(childFragmentManager, "LoadingDialog")
        } else {
            loadingDialog.dismiss()
        }
    }
}