package com.ominext.appchat.controller.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.ominext.appchat.model.ChatMessage
import com.ominext.appchat.model.Room
import com.ominext.appchat.view.dialog.showImage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapteMessage(val mListData: ArrayList<ChatMessage>, val context: Context, val idUser: String) :
    RecyclerView.Adapter<AdapteMessage.ViewHolder>() {
    val SENDER = 1
    val RECIPIENT = 0
    val SYSTEM=2

    override fun getItemCount(): Int {
        return mListData.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = mListData.get(position)

        return if (message.fromUser.equals(idUser)) {
            SENDER
        } else if (message.fromUser == "system")
            return SYSTEM
        else {
            RECIPIENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_to, parent, false)
            return ViewHolder(v as LinearLayout)
        }
        else if (viewType==2){
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_notification, parent, false)
            return ViewHolder(v as LinearLayout)
        }
        else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_from, parent, false)
            return ViewHolder(v as LinearLayout)
        }
    }


    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mListData.get(position)
        holder.ivImage.tag = data
        holder.tvText.text = data.content
        if(data.image!= ""){
            holder.ivImageMessage.visibility=View.VISIBLE
            holder.tvText.visibility=View.GONE
            Picasso.get().load(data.image).into(holder.ivImageMessage)
        }
        else {
            holder.ivImageMessage.visibility=View.GONE
            holder.tvText.visibility=View.VISIBLE

        }
        val time = (data.time)* 1000
        val sdf = SimpleDateFormat("MMM dd HH:mm").format(Date(time))
        holder.tvTime.text = (sdf)
        if (data.fromUser=="system") holder.ivImage.visibility=View.GONE
        loadImageUser(data.fromUser,holder.ivImage)
    }
    private fun loadImageUser(fromUser: String, ivImage: CircleImageView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(fromUser).get()
            .addOnSuccessListener {
                var image:String?
                if (fromUser=="system") image="nothing"
                else image= it.get("image") as String?
                if (image == ""){
                    return@addOnSuccessListener
                }
                Picasso.get().load(image).into(ivImage)

            }

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvText: TextView = itemView.findViewById(R.id.tv_message)
        var ivImage: CircleImageView = itemView.findViewById(R.id.iv_message_user)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var ivImageMessage: ImageView = itemView.findViewById(R.id.imageView_message_image)

        init {
            tvText.setOnClickListener {
                showTime()
            }
//            ivImageMessage.setOnClickListener {
////                val showImage = showImage(context,)
////                showImage.show()
//            }
            itemView.setOnClickListener {
                if (mListener != null) {
                    mListener!!.onClickFaceItem(ivImage.tag as ChatMessage)
                    showTime()
                }
            }
        }
        private fun showTime() {
            if (tvTime.visibility  == View.VISIBLE){
                tvTime.visibility  = View.GONE
            }else{
                tvTime.visibility  = View.VISIBLE
            }
        }

    }
    var mListener: OnClickFaceItemListener? = null

    fun setOnClickItemListener(event: OnClickFaceItemListener) {
        mListener = event
    }

    interface OnClickFaceItemListener {
        fun onClickFaceItem(data: ChatMessage)
    }

}
