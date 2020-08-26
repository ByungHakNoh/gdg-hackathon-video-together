package org.personal.videotogether.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData

class InvitationDialog : DialogFragment(), DialogInterface.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private val roomId by lazy { requireArguments().getInt("roomId") }
    private val inviterName by lazy { requireArguments().getString("inviterName") }
    private val youtubeData by lazy { requireArguments().getParcelable<YoutubeData>("youtubeData") }

    private lateinit var dialogListener: DialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context, R.style.AlertDialogCustom)
            .setTitle("유투브 같이보기 초대")
            .setMessage("$inviterName 이 유투브 같이보기에 초대했습니다\n수락하시겠습니까?")
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
                dialogListener.onConfirm(roomId, inviterName!!, youtubeData!! )
                dismiss()
            }
            cancelId -> dismiss()
        }
    }


    interface DialogListener {
        fun onConfirm(roomId: Int, inviterName: String, youtubeData: YoutubeData)
    }
}