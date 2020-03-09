package com.ominext.appchat.view.dialog

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.Window
import com.google.firebase.storage.FirebaseStorage
import com.ominext.appchat.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_show_imgae.*
import java.io.File


class showImage(context: Context, url: String) : Dialog(context, R.style.AppTheme) {
    val firebaseStorage = FirebaseStorage.getInstance().reference
    val httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url).name

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_show_imgae)
        Picasso.get().load(url).into(imageView_message_image)
        iv_back_image_massage.setOnClickListener {
            dismiss()
        }
        iv_download_file.setOnClickListener {
            downloadFile(context,httpsReference,".png",DIRECTORY_DOWNLOADS,url)

        }
    }

    fun downloadFile(context: Context, fileName: String, fileExtension: String, destinationDirectory: String, url: String) {

        val downloadmanager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension)

        downloadmanager.enqueue(request)
    }
}
