package org.personal.videotogether.util.view

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

class ViewHandler {

    fun handleClearTextBtn(textCount: Int, clearBtn: ImageButton) {
        if (textCount == 0) {
            clearBtn.visibility = View.GONE
        } else {
            clearBtn.visibility = View.VISIBLE
        }
    }

    fun handleWarningText(warningText: TextView) {
        warningText.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            warningText.visibility = View.INVISIBLE
        }, 1500)
    }
}