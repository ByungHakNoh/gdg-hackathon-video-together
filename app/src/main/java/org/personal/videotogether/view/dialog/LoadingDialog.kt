package org.personal.videotogether.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.personal.videotogether.R


class LoadingDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireParentFragment().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_loading, null)

        return AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
    }
}