@file:Suppress("UNREACHABLE_CODE")

package com.quang.appchat.controller.activity

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ominext.appchat.R
import com.ominext.appchat.controller.swipe.OnSwipeTouchListener
import com.ominext.appchat.model.Room
import com.ominext.appchat.view.dialog.progessbar
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.controller.adapter.AdapterAddMember
import com.quang.appchat.model.User
import kotlinx.android.synthetic.main.act_add_member_to_group.*
import kotlinx.android.synthetic.main.actionbar_member.*
import java.lang.ref.WeakReference
import java.lang.reflect.Field

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddMemberAct : BaseActivity() {
    private lateinit var recycler: RecyclerView
    lateinit var uidUser: String
    var mData: ArrayList<User> = ArrayList()
    val db = FirebaseFirestore.getInstance()
    internal var adapter: AdapterAddMember? = null

    internal var textlength = 0

    var array_sort: ArrayList<User> = ArrayList()
    val update = db.collection("users")
    val first = db.collection("users").limit(10)
    var myVariable = 10

    override fun initView() {
        val progessbar = progessbar(this)
        progessbar.show()
//        text_create_group.text = "Thêm Thành Viên"
        uidUser = getPred("KEY_USER").toString()
        recycler = findViewById(R.id.rv_add_member)
        addMember1(progessbar)
//        addMember(first)
        adapter = AdapterAddMember(mData, this)
        recycler.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        recycler.setHasFixedSize(true)
        recycler.adapter = adapter


        iv_back_member.setOnClickListener {
            finish()
//            overridePendingTransition(R.anim.slide_down, R.anim.slide_up)
        }
        btn_created.setOnClickListener {
            createGroup()
        }

        ln_AddMember.setOnClickListener { ln_AddMember.hideKeyboard() }
        searchMember()

    }

    private fun createGroup() {
        val ArrUser: ArrayList<String> = ArrayList()
        for (i in adapter!!.mListData) {
            if (i.isSelected) {
                ArrUser.add(i.uid)
            }
        }
        ArrUser.add(uidUser)
        checkRoom(ArrUser)
    }

    private fun checkRoom(ArrUser: ArrayList<String>) {
        val nameRoom: String = intent.getStringExtra("data")
        val refRoom = FirebaseFirestore.getInstance().collection("room").document()
        val readUserFirebase = db.collection("users")


        if (ArrUser.size >= 2) {
            db.collection("room").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val room: Room = document.toObject(Room::class.java)
                        val uIdMember: ArrayList<String> = room.uidMember
                        if (uIdMember.equals(ArrUser)) {
                            toast("The chat room already exists")
                            finish()
                            return@addOnSuccessListener
                        }
                    }
                    val arrAdmin: ArrayList<String> = ArrayList()
                    arrAdmin.add(uidUser)
                    val arrUserRead: ArrayList<String> = ArrayList()
                    arrUserRead.add(uidUser)
                    val lastContent = hashMapOf(
                        "content" to "No message",
                        "fromUser" to uidUser,
                        "userName" to ""
                    )
                    val room = hashMapOf(
                        "arrAdmin" to ArrayList(arrAdmin),
                        "lastContent" to lastContent,
                        "arrUserRead" to ArrayList(arrUserRead),
                        "nameRoom" to nameRoom,
                        "time" to System.currentTimeMillis() / 1000,
                        "image" to "https://firebasestorage.googleapis.com/v0/b/appchat-e2938.appspot.com/o/images%2Fb6efd5bb-d1fb-4294-aa47-c22c38ad6278?alt=media&token=816f428a-405f-48a2-9c83-aedba441cec1",
                        "uidMember" to ArrayList(ArrUser)
                    )



                    for (iUser in ArrUser) {
                        readUserFirebase.document(iUser).update("arrRoom", FieldValue.arrayUnion(refRoom.id))
                    }

                    val dataTimestamp = hashMapOf(
                        "idUser" to uidUser,
                        "idRoom" to refRoom.id,
                        "timestamp" to  System.currentTimeMillis() / 1000
                    )
                    db.collection("SeenMessageTimestamp").document().set(dataTimestamp)
                    
                    refRoom.set(room as Map<String, Any>)
                        .addOnSuccessListener {
                            toast("Success created room :   ${room.get("nameRoom")}")
                        }
                    ArrUser.clear()
                    finish()
                    return@addOnSuccessListener
                }
        } else {
            toast("Bạn phải chọn thành viên!!!")
        }
    }


    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun searchMember() {
        edt_search_member_bar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textlength = edt_search_member_bar.getText().length
                array_sort.clear()
                for (i in mData.indices) {
                    if (textlength <= mData.get(i).userName.length) {
                        Log.d("ertyyy", mData.get(i).userName.toLowerCase().trim())
                        if (mData.get(i).userName.toLowerCase().trim().contains(
                                edt_search_member_bar.text.toString().toLowerCase().trim({ it <= ' ' })
                            )
                        ) {
                            array_sort.add(mData.get(i))
                        }
                    }
                }
                adapter = AdapterAddMember(array_sort, this@AddMemberAct)
//                adapter?.AdapterAddMember(array_sort)
                recycler.setAdapter(adapter)
                recycler.setLayoutManager(LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false))
            }
        })
    }

    private fun addMember(first: Query) {
        first.get()
            .addOnSuccessListener { result ->
                if (result.size() == 0) return@addOnSuccessListener
                val lastVisible = result.documents[result.size() - 1]
                val next = db.collection("users")
                    .startAfter(lastVisible)
                    .limit(10)
                for (document in result) {
                    val user: User = document.toObject(User::class.java)
                    if (document.id != uidUser) {
                        mData.add(user)
                        recycler.adapter?.notifyDataSetChanged()
                    }
                }
//                addMember(next)

                rv_add_member.setOnTouchListener(object : OnSwipeTouchListener() {
                    override fun onSwipeTop() {
                        val task = MyAsyncTask(this@AddMemberAct)
                        task.execute(2)
                        addMember(next)
                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Error getting documents.", exception)
            }
    }

    private fun addMember1(progessbar:progessbar) {
        db.collection("users").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ListGroupAct.TAG, "listen:error", e)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                for (dc in snapshots.documentChanges) {
                    val user = dc.document.toObject(User::class.java)
                    if (dc.document.id != uidUser) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> mData.add(user)
                        }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }
                progessbar.dismiss()
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        status("online")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        status("offline")
//    }
//
//    private fun status(stt: String) {
//        db.collection("users").document(uidUser).update("status", stt)
//    }

    class MyAsyncTask internal constructor(context: AddMemberAct) : AsyncTask<Int, String, String?>() {

        private var resp: String? = null
        private val activityReference: WeakReference<AddMemberAct> = WeakReference(context)

        override fun onPreExecute() {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Int?): String? {
            publishProgress("Sleeping Started") // Calls onProgressUpdate()
            try {
                val time = params[0]?.times(1000)
                time?.toLong()?.let { Thread.sleep(it / 2) }
//                publishProgress("Half Time") // Calls onProgressUpdate()
                time?.toLong()?.let { Thread.sleep(it / 2) }
//                publishProgress("Sleeping Over") // Calls onProgressUpdate()
                resp = "Android was sleeping for " + params[0] + " seconds"
            } catch (e: InterruptedException) {
                e.printStackTrace()
                resp = e.message
            } catch (e: Exception) {
                e.printStackTrace()
                resp = e.message
            }

            return resp
        }


        override fun onPostExecute(result: String?) {

            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.progressBar.visibility = View.GONE
//            activity.textView.text = result.let { it }
            activity.myVariable = 100
        }

        override fun onProgressUpdate(vararg text: String?) {

            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

//            Toast.makeText(activity, text.firstOrNull(), Toast.LENGTH_SHORT).show()

        }
    }

    override fun setup() = Setup(TAG, R.layout.act_add_member_to_group)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    companion object {
        val EXTRA_DATA: String? = "EXTRA_DATA"
        val TAG: String = AddMemberAct::class.java.name
    }
}