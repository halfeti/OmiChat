package com.ominext.appchat.view.loading

import android.app.ProgressDialog
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity


 class YourAsyncTask(activity: AppCompatActivity) : AsyncTask<Void, Void, Void>() {
    private val dialog: ProgressDialog

    init {
        dialog = ProgressDialog(activity)
    }

    override fun onPreExecute() {
        dialog.setMessage("Doing something, please wait.")
        dialog.show()
    }

    override fun doInBackground(vararg args: Void): Void? {
        // do background work here
        return null
    }

    override fun onPostExecute(result: Void) {
        // do UI work here
        if (dialog.isShowing()) {
            dialog.dismiss()
        }
    }
}