package com.ominext.appchat.controller.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.ominext.appchat.controller.adapter.AdapterMemberInGroup
import com.ominext.appchat.model.Room
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.controller.activity.ChatAct
import com.quang.appchat.controller.activity.ListGroupAct
import com.quang.appchat.model.User
import com.wajahatkarim3.easyvalidation.core.view_ktx.contains
import kotlinx.android.synthetic.main.act_group_chat.*
import kotlinx.android.synthetic.main.act_member_in_group.*
import kotlinx.android.synthetic.main.actionbar_member.*
import kotlinx.android.synthetic.main.actionbar_message.*

class MemberInGroupAct : BaseActivity(), AdapterMemberInGroup.OnClickFaceItemListener {

    private lateinit var recycler_member_view: RecyclerView
    val uData: ArrayList<User> = ArrayList()
    var db = FirebaseFirestore.getInstance()
    var room: Room? = null
    lateinit var uidUser: String
    var myDialog: Dialog? = null
    var uIdMember: ArrayList<String>? = null
    lateinit var idRoom: String

    override fun initView() {
        uidUser = getPred("KEY_USER").toString()
        idRoom = intent.getStringExtra("KEY_ID_ROOM")
        text_create_group.text = "Thành Viên"
        readTeamDatabase(idRoom)
        myDialog = Dialog(this)

        val adapter_member_view = AdapterMemberInGroup(uData, idRoom, this)
        rcv_member_list.layoutManager = LinearLayoutManager(this)
        rcv_member_list.adapter = adapter_member_view
        adapter_member_view.setOnClickItemListener(this)
        recycler_member_view = findViewById(R.id.rcv_member_list)
        recycler_member_view.setHasFixedSize(true)

        iv_back_member.setOnClickListener { finish() }
    }

    override fun onClickFaceItem(data: User) {
        db.collection("room").document(idRoom).get()
            .addOnSuccessListener {
                if (!room?.arrAdmin.toString().contains(uidUser))
                    toast("Bạn không có quyền Quản trị viên")
                else {
                    val txtclose: TextView
                    val btncancel: Button
                    val btndelete: Button
                    val btnsetadmin: Button
                    val btnunsetadmin: Button
                    myDialog?.setContentView(R.layout.dialog_member_option)
                    txtclose = myDialog!!.findViewById(R.id.txt_close)
                    txtclose.text = "X"
                    btncancel = myDialog!!.findViewById(R.id.btn_cancel) as Button
                    btndelete = myDialog!!.findViewById(R.id.delete_member) as Button
                    btnsetadmin = myDialog!!.findViewById(R.id.set_admin) as Button
                    btnunsetadmin = myDialog!!.findViewById(R.id.unset_admin) as Button
                    txtclose.setOnClickListener { myDialog!!.dismiss() }
                    btncancel.setOnClickListener { myDialog!!.dismiss() }
                    btndelete.setOnClickListener {
                        deleteMember(data.uid)
                        myDialog!!.dismiss()
                    }
                    btnsetadmin.setOnClickListener {
                        setAdministrator(data.uid)
                        myDialog!!.dismiss()
                    }
                    btnunsetadmin.setOnClickListener {
                        unsetAdministrator(data.uid)
                        myDialog!!.dismiss()
                    }
                    myDialog!!.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog!!.show()
                }
            }
    }
    private fun deleteMember(id: String) {
        val uid = FirebaseDatabase.getInstance().getReference("/messages").push().key.toString()
        val timeMessage = System.currentTimeMillis() / 1000
        db.collection("users").document(id).update("arrRoom", FieldValue.arrayRemove(idRoom))
        db.collection("room").document(idRoom).update("uidMember", FieldValue.arrayRemove(id))
        val content = "bị xóa khỏi nhóm"
        contentMember(id, content, uid, timeMessage)

    }
    private fun contentMember(id: String, content: String, uid: String, time: Long) {
        db.collection("users").document(id).get().addOnSuccessListener {
            val name = it["userName"] as String
            val message = hashMapOf(
                "content" to "$name $content ",
                "time" to time,
                "image" to "",
                "fromUser" to "system",
                "toRoom" to idRoom
            )
            db.collection("message").document(idRoom).collection("msg").document(uid)
                .set(message as Map<String, Any>)
                .addOnSuccessListener {
                    Log.i(TAG, "DocumentSnapshot added with ID: $it")
                }
                .addOnFailureListener { e ->
                    toast("Error adding document: $e")
                    Log.i(TAG, "Error adding document", e)
                }
            val arrUserRead: ArrayList<String> = ArrayList()
            arrUserRead.add(uidUser)
            val lastContent = hashMapOf(
                "content" to " đã xóa $name khỏi nhóm",
                "fromUser" to uidUser,
                "userName" to name
            )
            db.collection("room").document(idRoom).update("lastContent", lastContent)
            db.collection("room").document(idRoom).update("arrUserRead", ArrayList(arrUserRead))
        }
    }

    private fun setAdministrator(id: String) {
        val uid = FirebaseDatabase.getInstance().getReference("/messages").push().key.toString()
        val timeMessage = System.currentTimeMillis() / 1000

        db.collection("room").document(idRoom).get().addOnSuccessListener {
            if (!room?.arrAdmin.toString().contains(id)) {
                db.collection("room").document(idRoom).update("arrAdmin", FieldValue.arrayUnion(id))

                db.collection("users").document(id).get().addOnSuccessListener {
                    val name = it["userName"] as String
                    val message = hashMapOf(
                        "content" to " $name set làm quản trị viên",
                        "time" to timeMessage,
                        "image" to "",
                        "fromUser" to "system",
                        "toRoom" to idRoom
                    )
                    db.collection("message").document(idRoom).collection("msg").document(uid)
                        .set(message as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.i(TAG, "DocumentSnapshot added with ID: $it")
                        }
                        .addOnFailureListener { e ->
                            toast("Error adding document: $e")
                            Log.i(TAG, "Error adding document", e)
                        }
                    val arrUserRead: ArrayList<String> = ArrayList()
                    arrUserRead.add(uidUser)
                    val lastContent = hashMapOf(
                        "content" to " set $name làm quản trị viên",
                        "fromUser" to uidUser,
                        "userName" to name
                    )
                    db.collection("room").document(idRoom).update("lastContent", lastContent)
                    db.collection("room").document(idRoom).update("arrUserRead", ArrayList(arrUserRead))
                }
            } else toast("Đã là Quản trị viên")
        }
    }
    private fun unsetAdministrator(id: String) {
        val uid = FirebaseDatabase.getInstance().getReference("/messages").push().key.toString()
        val timeMessage = System.currentTimeMillis() / 1000
        db.collection("room").document(idRoom).get().addOnSuccessListener {
            if (room?.arrAdmin.toString().contains(id)) {
                db.collection("room").document(idRoom).update("arrAdmin", FieldValue.arrayRemove(id))

                db.collection("users").document(id).get().addOnSuccessListener {
                    val name = it["userName"] as String
                    val message = hashMapOf(
                        "content" to "$name bị đẩy xuống làm thành viên",
                        "time" to timeMessage,
                        "image" to "",
                        "fromUser" to "system",
                        "toRoom" to idRoom
                    )
                    db.collection("message").document(idRoom).collection("msg").document(uid)
                        .set(message as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.i(TAG, "DocumentSnapshot added with ID: $it")
                        }
                        .addOnFailureListener { e ->
                            toast("Error adding document: $e")
                            Log.i(TAG, "Error adding document", e)
                        }
                    val arrUserRead: ArrayList<String> = ArrayList()
                    arrUserRead.add(uidUser)
                    val lastContent = hashMapOf(
                        "content" to " đã đẩy $name xuống làm thành viên",
                        "fromUser" to uidUser,
                        "userName" to name
                    )
                    db.collection("room").document(idRoom).update("lastContent", lastContent)
                    db.collection("room").document(idRoom).update("arrUserRead", ArrayList(arrUserRead))

                }
            } else toast("Chưa là Quản trị viên")
        }

    }

    private fun getMemberView(uIdMember: ArrayList<String>) {
        db.collection("users").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ListGroupAct.TAG, "listen:error", e)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                for (dc in snapshots.documentChanges) {
                    val user = dc.document.toObject(User::class.java)
                    if (uIdMember.contains(dc.document.id)) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> uData.add(user)
                            DocumentChange.Type.MODIFIED -> modifiedMemberView(user)
                            DocumentChange.Type.REMOVED -> removedMemberView(user)
                        }
                    }
                    recycler_member_view.adapter?.notifyDataSetChanged()
                }
            }
        }
    }
    private fun removedMemberView(user: User) {
//        for (i in 0 until uData.size) {
//            if (uData[i].uid.equals(user.uid)) {
//                uData.remove(uData[i])
//            }
//        }
    }

    private fun modifiedMemberView(user: User) {
//        for (i in 0 until uData.size) {
//            if (uData[i].uid.equals(user.uid)) {
//                uData[i] = user
//            }
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun readTeamDatabase(idRoom: String) {
        db.collection("room").document(idRoom)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ListGroupAct.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                    uData.clear()
                else
                    "Server"
                if (snapshot != null && snapshot.exists()) {
                    room = snapshot.toObject(Room::class.java)
                    room!!.idRoom = idRoom
                    uIdMember = room!!.uidMember
                    getMemberView(uIdMember!!)
                } else {
                    Log.d(TAG, "$source data: null")
                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }
    override fun setup() = Setup(TAG, R.layout.act_member_in_group)

    companion object {
        private val TAG: String = MemberInGroupAct::class.java.name
    }

    override fun onResume() {
        super.onResume()
        status("on")
    }

    override fun onPause() {
        super.onPause()
        status("off")
    }

    private fun status(stt: String) {
        db.collection("users").document(uidUser).update("status", stt)
    }
}