package com.quang.appchat.controller.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ominext.appchat.R
import com.ominext.appchat.controller.activity.ProfileActivity
import com.ominext.appchat.controller.swipe.SwipeController
import com.ominext.appchat.controller.swipe.SwipeControllerActions
import com.ominext.appchat.model.Room
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.controller.adapter.AdapterListGroup
import com.quang.appchat.model.User
import kotlinx.android.synthetic.main.act_list_group_chat.*
import kotlinx.android.synthetic.main.actionbarbottom_common.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNREACHABLE_CODE", "INCOMPATIBLE_ENUM_COMPARISON", "UNCHECKED_CAST", "NON_EXHAUSTIVE_WHEN")
class ListGroupAct : BaseActivity(), AdapterListGroup.OnClickFaceItemListener {

    private val KEY_ID_ROOM: String = "KEY_ID_ROOM"
    private val KEY_UID_USER: String = "KEY_UID_USER"

    private lateinit var recycler: RecyclerView
    var adapter: AdapterListGroup? = null

    private val listRoom: ArrayList<Room> = ArrayList()
    private val listSortRoom: ArrayList<Room> = ArrayList()

    var myDialog: Dialog? = null
    lateinit var uidUser: String

    internal var swipeController: SwipeController? = null

    var db = FirebaseFirestore.getInstance()
    var ref = db.collection("room")

    var isFirstListLoaded: Boolean = true


    override fun onClickFaceItem(data: Room) {
        val i = Intent(this, ChatAct::class.java)
        i.putExtra(KEY_ID_ROOM, data.idRoom)
        i.putExtra(KEY_UID_USER, uidUser)
        startActivityForResult(i, PICK_CONTACT_REQUEST)
    }


    override fun initView() {
        ln_home.setBackgroundColor(Color.parseColor("#FFFD7129"))
//        uidUser = intent.getStringExtra("KEY_UID_USER")
        uidUser = getPred("KEY_USER").toString()
        recycler = findViewById(R.id.rv_list_group)
        recycler.setHasFixedSize(true)
        adapter = AdapterListGroup(listRoom, this, uidUser)
        adapter!!.setOnClickItemListener(this)
        recycler.adapter = adapter
        setupRecyclerView()
        iv_create_new_group.setOnClickListener {
            val i = Intent(this, CreateNewGroupAct::class.java)
            startActivity(i)
        }

        btn_profile.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            startActivity(i)
        }
        listenerAddRoom()
    }


    private fun listenerAddRoom() {
        val readUserFirebase = db.collection("users").document(uidUser)
        val readRoom = db.collection("room")
        readUserFirebase.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                listRoom.clear()
            else
                "Server"

            //read user roomArr, idRoom realTime
            if (snapshot != null && snapshot.exists()) {
                val roomArr: ArrayList<String> = snapshot.data?.get("arrRoom") as ArrayList<String>
                if (roomArr.size == 0) progressBar.visibility = View.GONE
                listRoom.clear()
                for (arrRoom in roomArr) {
                    readRoom.addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Log.w(TAG, "listen:error", e)
                            return@addSnapshotListener
                        }
                        if (snapshots != null) {
                            for (dc in snapshots.documentChanges) {
                                val room = dc.document.toObject(Room::class.java)
                                room.idRoom = dc.document.id
                                if (arrRoom.equals(dc.document.id)) {
                                    when (dc.type) {
                                        DocumentChange.Type.ADDED -> listRoom.add(room)
                                        DocumentChange.Type.MODIFIED -> modifiedItemList(dc.document.id, room)
                                        DocumentChange.Type.REMOVED -> removeItemList(dc.document.id, room)
                                    }
                                }
                                listRoom.sortWith(object : Comparator<Room> {
                                    override fun compare(o1: Room, o2: Room): Int {
                                        return ((o2.time - o1.time).toInt())
                                    }
                                })
                                Log.i(TAG, "listRoomSort: ${listRoom}")
                                progressBar.visibility = View.GONE
                                recycler.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "$source data: null")
            }
        }
    }

    private fun removeItemList(id: String, room: Room) {
//        for (i in 0 until listRoom.size)
//        {
//            if (listRoom[i].idRoom.equals(id)){
//                listRoom[i].nameRoom.
//                Log.e("NUMBER", listRoom[i].toString())
//            }
//        }
    }

    private fun modifiedItemList(id: String, room: Room) {
        for (i in 0 until listRoom.size) {
            if (listRoom[i].idRoom.equals(id)) {
                listRoom[i] = room
                Log.e("NUMBER", listRoom[i].toString())
            }
        }
    }

    override fun setup() = Setup(TAG, R.layout.act_list_group_chat)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressBar.visibility = View.VISIBLE
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        initView()
    }

    companion object {
        val PICK_CONTACT_REQUEST = 1
        val TAG: String = ListGroupAct::class.java.name
    }

    private fun setupRecyclerView() {
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter

        swipeController = SwipeController(object : SwipeControllerActions() {
            override fun onRightClicked(position: Int) {
                val idRoom = adapter?.mListData!![position].idRoom
                val nameRoom = adapter!!.mListData[position].nameRoom
                db.collection("room").document(idRoom).get().addOnSuccessListener {
                    val room = it.toObject(Room::class.java)
                    if (room != null) {
                        for (i in room.arrAdmin) {
                            if (i.equals(uidUser)) {
                                val uid = it["uidMember"] as ArrayList<String>
                                removeIdRoomInUser(idRoom, uid)
                                removeIdUserInRoom(idRoom)
                                toast("Xóa nhóm thành công: $nameRoom")
                                recycler.adapter!!.notifyDataSetChanged()
                                return@addOnSuccessListener
                            } else {
                                toast("Bạn không phải là admin")
                            }
                        }
                    }
                }
            }

        })

        val itemTouchhelper = ItemTouchHelper(swipeController!!)
        itemTouchhelper.attachToRecyclerView(recycler)

        recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController!!.onDraw(c)
            }
        })
    }

    private fun removeIdUserInRoom(idRoom: String) {
        db.collection("room").document(idRoom).delete()
    }

    private fun removeIdRoomInUser(idRoom: String, arrUser: ArrayList<String>) {
        for (idUser in arrUser) {
            db.collection("users").document(idUser)
                .update("arrRoom", FieldValue.arrayRemove(idRoom))
        }
    }

    override fun onBackPressed() {}
//
//    override fun onResume() {
//        super.onResume()
//        status("online")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        status("offline")
//    }

    private fun status(stt: String) {
        db.collection("users").document(uidUser).update("status", stt)
    }


}