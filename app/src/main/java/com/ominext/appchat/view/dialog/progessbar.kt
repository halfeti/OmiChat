package com.ominext.appchat.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import com.ominext.appchat.R

class progessbar(context: Context) : Dialog(context, R.style.dialog_theme){

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_progress_bar_with_text)
    }



}

