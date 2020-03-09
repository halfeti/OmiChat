package com.quang.appchat.controller.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ominext.appchat.R
import com.ominext.appchat.model.Room
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*


class AdapterListGroup(val mListData: ArrayList<Room>, val context: Context, val idUser: String) :
    RecyclerView.Adapter<AdapterListGroup.ViewHolder>() {

    override fun getItemCount(): Int = mListData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list_group_chat, parent, false)
        return ViewHolder(itemView)
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mListData.get(position)
        holder.tvNamegroup.tag = data
        holder.tvNamegroup.text = data.nameRoom

        val time = data.time * 1000
        val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(time))
        holder.tvTime.text = sdf

        val content = data.lastContent["content"]
        val fromUser = data.lastContent["fromUser"]
        val nameUser = data.lastContent["userName"]

        for (i in data.arrUserRead) {
            if (fromUser != null) {
                if (content != "" && fromUser.equals(idUser)) {
                    holder.tvContent.text = "Bạn: $content"
                }else if (content == "" && fromUser.equals(idUser)){
                    holder.tvContent.text = "Bạn: send image"
                }else if (content != "" && !fromUser.equals(idUser) ){
                    if (i.equals(idUser)){
                        holder.tvContent.text = "$nameUser: $content"
                        holder.tvContent.setTextColor(R.color.colorBlack)
                        holder.tvContent.typeface = Typeface.DEFAULT
                        return
                    } else {
                        holder.tvContent.text = "$nameUser: $content"
                        holder.tvContent.setTextColor(R.color.sky_blue)
                        holder.tvContent.typeface = Typeface.DEFAULT_BOLD
                    }
                }else if (content == "" && !fromUser.equals(idUser)){
                    if (i.equals(idUser)){
                        holder.tvContent.text = "$nameUser: send image"
                        holder.tvContent.setTextColor(R.color.colorBlack)
                        holder.tvContent.typeface = Typeface.DEFAULT
                        return
                    }else{
                        holder.tvContent.text = "$nameUser: send image"
                        holder.tvContent.setTextColor(R.color.sky_blue)
                        holder.tvContent.typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }else holder.tvContent.text = "No message"
        }
        if (data.image == "") return
        Picasso.get().load(data.image).into(holder.ivImage)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvNamegroup: TextView
        var tvTime: TextView
        var ivImage: CircleImageView
        var tvContent: TextView

        init {
            tvNamegroup = itemView.findViewById(R.id.tv_name_group)
            tvTime = itemView.findViewById(R.id.tv_time)
            tvContent = itemView.findViewById(R.id.tv_content)
            ivImage = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener {
                if (mListener != null) {
                    mListener!!.onClickFaceItem(tvNamegroup.tag as Room)
                }
            }
        }

    }

    var mListener: OnClickFaceItemListener? = null
    fun setOnClickItemListener(event: OnClickFaceItemListener) {
        mListener = event
    }

    interface OnClickFaceItemListener {
        fun onClickFaceItem(data: Room)
    }
}



