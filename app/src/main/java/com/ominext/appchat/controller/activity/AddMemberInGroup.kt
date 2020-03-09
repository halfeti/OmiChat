package com.ominext.appchat.controller.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.ominext.appchat.controller.adapter.AdapterAddMemberInGroup
import com.ominext.appchat.model.Room
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.controller.activity.ListGroupAct
import com.quang.appchat.model.User
import com.wajahatkarim3.easyvalidation.core.view_ktx.contains
import kotlinx.android.synthetic.main.act_member_in_group_add.*
import kotlinx.android.synthetic.main.actionbar_member.*

class AddMemberInGroup : BaseActivity() {
    private lateinit var recycler_member_to_add: RecyclerView
    var auData: ArrayList<User> = ArrayList()
    var adapter_member_to_add: AdapterAddMemberInGroup? = null
    var array_sort: ArrayList<User> = ArrayList()
    var room: Room? = null
    lateinit var uidUser: String
    var uIdMember: ArrayList<String>? = null
    lateinit var idRoom: String


    internal var textlength = 0

    var db = FirebaseFirestore.getInstance()

    override fun initView() {
        uidUser = getPred("KEY_USER").toString()
        idRoom = intent.getStringExtra("KEY_ID_ROOM")
        text_create_group.text = "Thêm Thành Viên"

        readTeamDatabase(idRoom)
        searchMember()

        adapter_member_to_add = AdapterAddMemberInGroup(auData, this)
        rcv_member_list_to_add.layoutManager = LinearLayoutManager(this)
        rcv_member_list_to_add.adapter = adapter_member_to_add
        recycler_member_to_add = findViewById(R.id.rcv_member_list_to_add)
        recycler_member_to_add.setHasFixedSize(true)


        iv_back_member.setOnClickListener { finish() }
        //get member added
        confirm_add_member_in_group.setOnClickListener {
            getMemberSellected()
        }
        ln_member_add_to_group.setOnClickListener { ln_member_add_to_group.hideKeyboard()}
        rcv_member_list_to_add.setOnClickListener { rcv_member_list_to_add.hideKeyboard() }
    }
    private fun searchMember() {
        edt_search_member_to_add.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textlength = edt_search_member_to_add.getText().length
                array_sort.clear()
                for (i in auData.indices) {
                    if (textlength <= auData[i].userName.length) {
                        if (auData[i].userName.toLowerCase().trim().contains(
                                edt_search_member_to_add.text.toString().toLowerCase().trim({ it <= ' ' })
                            )
                        ) {
                            array_sort.add(auData[i])
                        }
                    }
                }
                adapter_member_to_add = AdapterAddMemberInGroup(array_sort, this@AddMemberInGroup)
                recycler_member_to_add.setAdapter(adapter_member_to_add)
                recycler_member_to_add.setLayoutManager(
                    LinearLayoutManager(
                        applicationContext,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                )
            }
        })
    }

    private fun addMemberToGroupView(uIdMember: ArrayList<String>) {
        db.collection("users").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ListGroupAct.TAG, "listen:error", e)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                for (dc in snapshots.documentChanges) {
                    val user = dc.document.toObject(User::class.java)
                    if (!uIdMember.contains(dc.document.id)) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> auData.add(user)
                            DocumentChange.Type.MODIFIED -> Log.i(TAG, "Modified")
                            DocumentChange.Type.REMOVED -> Log.i(TAG, "Remove")
                        }
                    }
                    recycler_member_to_add.adapter?.notifyDataSetChanged()
                }
            }
        }
    }    @SuppressLint("SetTextI18n")
    private fun readTeamDatabase(idRoom: String) {
        db.collection("room").document(idRoom)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ListGroupAct.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                    auData.clear()
                else
                    "Server"
                if (snapshot != null && snapshot.exists()) {
                    room = snapshot.toObject(Room::class.java)
                    room!!.idRoom = idRoom
                    uIdMember = room!!.uidMember
                    addMemberToGroupView(uIdMember!!)
                } else {
                    Log.d(TAG, "$source data: null")
                }
            }
    }
    private fun getMemberSellected() {
        val arrUid: ArrayList<String> = ArrayList()
        for (i in adapter_member_to_add!!.mListData) {
            if (i.isSelected) {
                arrUid.add(i.uid)
            }
        }
        if (arrUid.size > 0){
            for (i in arrUid){
                val uid = FirebaseDatabase.getInstance().getReference("/messages").push().key.toString()
                val timeMessage = System.currentTimeMillis() / 1000
                db.collection("room").document(idRoom).update("uidMember", FieldValue.arrayUnion(i))
                db.collection("users").document(i).update("arrRoom", FieldValue.arrayUnion(idRoom))
                db.collection("users").document(i).get().addOnSuccessListener {
                    val name = it["userName"] as String
                    val message = hashMapOf(
                        "content" to "$name đã được thêm vào nhóm",
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
                        "content" to "$name đã được thêm vào nhóm",
                        "fromUser" to uidUser,
                        "userName" to name
                    )
                    db.collection("room").document(idRoom).update("lastContent", lastContent)
                    db.collection("room").document(idRoom).update("arrUserRead", ArrayList(arrUserRead))
                }
            }
            toast("Thêm thành công")
        }else{
            toast("Bạn phải chọn thành viên!!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }
    override fun setup() = Setup(TAG, R.layout.act_member_in_group_add)

    companion object {
        private val TAG: String = AddMemberInGroup::class.java.name
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