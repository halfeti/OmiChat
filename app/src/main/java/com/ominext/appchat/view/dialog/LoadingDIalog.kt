package com.quang.appchat.view.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.widget.ProgressBar

/**
 * Created by Dwi Ariyanto on 4/14/18.
 */

class LoadingDialog {
    private var dialog: AlertDialog? = null
    val isVisible: Boolean
        get() = dialog?.isShowing ?: false

    fun show(context: Context) {
        createIfNull(context)
        dialog?.show()
    }

    private fun createIfNull(context: Context) {
        if (dialog != null) return
        dialog = AlertDialog.Builder(context)
            .apply {
                setView(ProgressBar(context))
                setCancelable(false)
            }
            .create()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
