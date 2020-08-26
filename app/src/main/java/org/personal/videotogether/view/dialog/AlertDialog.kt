package org.personal.videotogether.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import org.personal.videotogether.R

class AlertDialog : DialogFragment(), DialogInterface.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var dialogListener: DialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = requireArguments().getString("title")
        val message = requireArguments().getString("message")

        return AlertDialog.Builder(context, R.style.AlertDialogCustom)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", this)
            .setNegativeButton("취소", this)
            .create()
    }

    // 다이얼로그에서 액티비티로 데이터 전송
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            dialogListener = requireParentFragment() as DialogListener

        } catch (e: ClassCastException) {

            e.printStackTrace()
            Log.i(TAG, "onAttach : 인터페이스 implement 안함")
        }
    }

    override fun onClick(p0: DialogInterface?, which: Int) {
        val confirmId = -1
        val cancelId = -2

        when (which) {
            confirmId -> {
                dialogListener.onConfirm()
                dismiss()
            }
            cancelId -> dismiss()
        }
    }

    interface DialogListener {
        fun onConfirm()
    }
}