package com.ominext.appchat.controller.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.quang.appchat.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AdapterAddMemberInGroup(val mListData: ArrayList<User>, val context: Context) :
    RecyclerView.Adapter<AdapterAddMemberInGroup.ViewHolder>() {

    override fun getItemCount(): Int {
        return mListData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_member_to_add, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mListData.get(position)
        holder.tvNameMember.text = data.userName
        holder.tvIdMember.text = data.uid

        holder.check.isChecked = data.isSelected
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
        if (data.image == "") return
        Picasso.get().load(data.image).into(holder.ivImage)

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvNameMember: TextView
        var tvIdMember: TextView
        var ivImage: CircleImageView
        var check: CheckBox

        init {
            tvNameMember = itemView.findViewById(R.id.tv_name_member_ingroup)
            tvIdMember = itemView.findViewById(R.id.tv_id_member_ingroup)
            ivImage = itemView.findViewById(R.id.iv_image_add_member_ingroup)
            check = itemView.findViewById(R.id.checkbox_ingroup)
        }
    }

}