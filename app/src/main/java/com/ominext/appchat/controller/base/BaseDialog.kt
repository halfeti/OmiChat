package com.dwiariyanto.androidkotlinsimpledemo.core.dialog

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View

/**
 * Created by Dwi Ariyanto on 4/5/18.
 */

abstract class BaseDialog(private val pLayoutId: Int, private val pCancelable: Boolean = false) {
    protected var dialog: AlertDialog? = null
    protected lateinit var layout: View

    protected abstract fun onShowDialog()
    protected abstract fun bindView(pLayout: View)

    protected fun showDialog(pContext: Context) {
        createIfNull(pContext)
        onShowDialog()
        dialog?.show()
    }

    private fun createIfNull(context: Context) {
        if (dialog != null) return

        val lLayout = LayoutInflater.from(context)
                .inflate(pLayoutId, null, false)
                .also {
                    layout = it
                    bindView(it)
                }

        dialog = AlertDialog.Builder(context)
                .apply {
                    setView(lLayout)
                    setCancelable(pCancelable)
                }
                .create()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

}