package org.personal.videotogether.util.view

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import org.personal.videotogether.domianmodel.UserData

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
            warningText.visibility = View.GONE
        }, 1500)
    }

    fun formChatRoomName(participantList: List<UserData>, myUserId: Int): String {
        val stringBuilder = StringBuilder()
        var isFirstName = true

        participantList.forEach { participant ->
            if (myUserId != participant.id) {

                if (isFirstName) {

                    stringBuilder.append(participant.name)
                    isFirstName = false

                } else {

                    stringBuilder.append(", ").append(participant.name)
                }
            }
        }
        return stringBuilder.toString()
    }
}