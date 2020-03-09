package com.ominext.appchat.controller.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.ominext.appchat.model.ChatMessage
import com.ominext.appchat.model.Room
import com.quang.appchat.controller.adapter.AdapterAddMember
import com.quang.appchat.model.User
import com.squareup.picasso.Picasso
import com.wajahatkarim3.easyvalidation.core.view_ktx.contains
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.member_in_group_item.view.*

class AdapterMemberInGroup (val mListData: ArrayList<User>, val idRoom: String, val context: Context) :
    RecyclerView.Adapter<AdapterMemberInGroup.ViewHolder>(){
    var db = FirebaseFirestore.getInstance()
//    private var isChat:Boolean=false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.member_in_group_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mListData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        p0?.tvMemberType?.text = mListData.get(p1)
        val data = mListData.get(position)
        holder.tvNameMember.tag = data
        holder.tvNameMember.text = data.userName
        val dc =db.collection("room").document(idRoom)
            dc.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val room= snapshot.toObject(Room::class.java)
                    if (room != null) {
                        if (room.arrAdmin.contains(data.uid))
                            holder.tvRole.text="Quản trị viên"
                        else holder.tvRole.text="Thành viên"
                    }
                }
            }
        db.collection("users").document(data.uid)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException!=null) return@addSnapshotListener
                if (documentSnapshot!= null){
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user!= null){
                        if (user.status=="on"){
                            holder.ivOnl.visibility=View.VISIBLE
                            holder.ivOff.visibility=View.GONE
                        }
                        else if (user.status=="off"){
                            holder.ivOnl.visibility=View.GONE
                            holder.ivOff.visibility=View.VISIBLE
                        }
                    }
                }
            }
        if (data.image == "") return
        Picasso.get().load(data.image).into(holder.ivImage)
    }

    fun removeAt(position: Int) {
        mListData.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvNameMember: TextView
        var tvRole: TextView
        var ivImage: CircleImageView
        var ivOnl: CircleImageView
        var ivOff: CircleImageView

        init {
            tvNameMember=itemView.findViewById(R.id.tv_name_member_ingroup)
            tvRole=itemView.findViewById(R.id.tv_role_ingroup)
            ivImage=itemView.findViewById(R.id.iv_image_member_ingroup)
            ivOnl=itemView.findViewById(R.id.onl)
            ivOff=itemView.findViewById(R.id.off)

            itemView.setOnClickListener {
                if (mListener != null) {
                    mListener!!.onClickFaceItem(tvNameMember.tag as User)
                }
            }
        }

    }

    var mListener: OnClickFaceItemListener? = null

    fun setOnClickItemListener(event: OnClickFaceItemListener) {
        mListener = event
    }

    interface OnClickFaceItemListener {
        fun onClickFaceItem(data: User)
    }
}