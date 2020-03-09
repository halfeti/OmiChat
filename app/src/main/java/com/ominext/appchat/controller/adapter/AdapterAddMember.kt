package com.quang.appchat.controller.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import com.ominext.appchat.R
import com.ominext.appchat.controller.Interface.LoadingMore
import com.quang.appchat.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class AdapterAddMember(var mListData: ArrayList<User>, val context: Context) :
    RecyclerView.Adapter<AdapterAddMember.ViewHolder>() {

    override fun getItemCount(): Int {
        return mListData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mListData[position]
        holder.tvNameMember.text = user.userName
        holder.tvIdMember.text = user.uid

        holder.check.isChecked = user.isSelected
        holder.check.tag = mListData[position]
        holder.check.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mListData[position].isSelected) {
                    mListData[position].isSelected = false
                } else {
                    mListData[position].isSelected = true
                }
            }
        })
        if (user.image == "") return
        Picasso.get().load(user.image).into(holder.ivImage)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvNameMember: TextView
        var tvIdMember: TextView
        var ivImage: CircleImageView
        var check: CheckBox


        init {
            tvNameMember = itemView.findViewById(R.id.tv_name_member)
            tvIdMember = itemView.findViewById(R.id.tv_id_member)
            ivImage = itemView.findViewById(R.id.iv_image_add_member)
            check = itemView.findViewById(R.id.checkbox)
        }

    }
}






