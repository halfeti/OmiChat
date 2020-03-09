package com.quang.appchat.controller.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ominext.appchat.R
import com.ominext.appchat.controller.activity.AddMemberInGroup
import com.ominext.appchat.controller.activity.MemberInGroupAct
import com.ominext.appchat.controller.adapter.AdapteMessage
import com.ominext.appchat.model.ChatMessage
import com.ominext.appchat.model.Room
import com.ominext.appchat.view.dialog.progessbar
import com.ominext.appchat.view.dialog.showImage
import com.quang.appchat.base.BaseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.act_group_chat.*
import kotlinx.android.synthetic.main.actionbar_message.*
import kotlinx.android.synthetic.main.actionbar_send_message.*
import kotlinx.android.synthetic.main.item_image_message.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatAct : BaseActivity(), AdapteMessage.OnClickFaceItemListener {

    val KEY_ID_ROOM: String = "KEY_ID_ROOM"
    private lateinit var recycler: RecyclerView
    val mData: ArrayList<Room> = ArrayList()
    val listDataMessage: ArrayList<ChatMessage> = ArrayList()

    var db = FirebaseFirestore.getInstance()

    var room: Room? = null
    lateinit var uidUser: String
    lateinit var idRoom: String
    var myDialog: Dialog? = null

    override fun initView() {
        uidUser = getPred("KEY_USER").toString()
        idRoom = intent.getStringExtra("KEY_ID_ROOM")
        readTeamDatabase(idRoom)
        loadImageGroup(idRoom)

        ln_Chat.setOnClickListener { ln_Chat.hideKeyboard() }

        iv_back.setOnClickListener {
            onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

//        getMessage()
        //Pick to change image group
        avatar_in_group.setOnClickListener {
            Log.d("Chat Group", "Try to show avatar group photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        //recycle message
        recycler = findViewById(R.id.rc_message)
        recycler.setHasFixedSize(true)
        val adapter = AdapteMessage(listDataMessage, this, uidUser)
        recycler.adapter = adapter
        adapter.setOnClickItemListener(this)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler.layoutManager = linearLayoutManager


        iv_send_message.setOnClickListener {
            val edtMessage = edt_send_message.text.toString().trim()
            if (edtMessage.equals("")) {
                return@setOnClickListener
            } else {
                val text = edt_send_message.text
                edt_send_message.text = Editable.Factory.getInstance().newEditable("")
                saveMessageToDatabase(text.toString().trim(), "", 0, 0)
                linearLayoutManager.smoothScrollToPosition(recycler, null, adapter.itemCount)
            }
        }

        //dropdown option list
        if (iv_drop_down.visibility == View.VISIBLE && iv_drop_up.visibility == View.GONE) {
            iv_drop_down.setOnClickListener {
                iv_drop_down.visibility = View.GONE
                iv_drop_up.visibility = View.VISIBLE
                option_group.visibility = View.VISIBLE
                type_bar.visibility = View.GONE
                ln_member_ingroup.setBackgroundColor(Color.parseColor("#FF7F3D"))
                ln_out_group.setBackgroundColor(Color.parseColor("#FF7F3D"))
                ln_add_member.setBackgroundColor(Color.parseColor("#FF7F3D"))
                iv_drop_down.hideKeyboard()
            }
        }
        iv_drop_up.setOnClickListener {
            iv_drop_down.visibility = View.VISIBLE
            type_bar.visibility = View.VISIBLE
            iv_drop_up.visibility = View.GONE
            option_group.visibility = View.GONE
        }

        //drop down member view
        ln_member_ingroup.setOnClickListener {
            val i = Intent(this, MemberInGroupAct::class.java)
            i.putExtra(KEY_ID_ROOM, idRoom)
            startActivity(i)
        }

        //drop down to add member
        ln_add_member.setOnClickListener {
            val i = Intent(this, AddMemberInGroup::class.java)
            i.putExtra(KEY_ID_ROOM, idRoom)
            startActivity(i)
        }

        //Dialog confirm out group
        myDialog = Dialog(this)

        //select image to send
        iv_send_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }


    //click image
    override fun onClickFaceItem(data: ChatMessage) {
        if (data.image != "") {
            val showImage = showImage(this, data.image)
            showImage.show()
            Log.i(TAG, "Url Image" + data.image)
        }
    }

    override fun onStart() {
        super.onStart()
        db.collection("room").document(idRoom).update("arrUserRead", FieldValue.arrayUnion(uidUser))
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
                    "local"
                else
                    "Server"
                if (snapshot != null && snapshot.exists()) {
                    val room = snapshot.toObject(Room::class.java)
                    room!!.idRoom = idRoom
                    mData.add(room)
                    tv_name_room.text = room.nameRoom
                    val size: Int = room.uidMember.size
                    tv_size_member.text = "$size Người Tham gia"

                } else {
                    Log.d(TAG, "$source data: null")
                }
            }
    }

    override fun setup() = Setup(TAG, R.layout.act_group_chat)

    companion object {
        private val TAG: String = ChatAct::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        initView()
        readRealTimeMessage()

    }


    private fun readRealTimeMessage() {
        val first = db.collection("message").document(idRoom).collection("msg")
        first.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ListGroupAct.TAG, "listen:error", e)
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                val chat = dc.document.toObject(ChatMessage::class.java)

                if (chat.toRoom == idRoom) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> listDataMessage.add(chat)
                    }
                    listDataMessage.sortWith(object : Comparator<ChatMessage> {
                        override fun compare(o1: ChatMessage, o2: ChatMessage): Int {
                            return ((o1.time - o2.time).toInt())
                        }
                    })
                    recycler.adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    private fun saveMessageToDatabase(text: String, urlImage: String?, newHeight: Int, newWidth: Int) {
        val uid = FirebaseDatabase.getInstance().getReference("/messages").push().key.toString()
        val timeMessage = System.currentTimeMillis() / 1000
        val message = hashMapOf(
            "content" to text,
            "image" to urlImage,
            "time" to timeMessage,
            "fromUser" to uidUser,
            "toRoom" to idRoom,
            "height" to newHeight,
            "width" to newWidth
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

        db.collection("users").document(uidUser).get().addOnSuccessListener {
            val name = it["userName"] as String
            val arrUserRead: ArrayList<String> = ArrayList()
            arrUserRead.add(uidUser)
            val lastContent = hashMapOf(
                "content" to text,
                "fromUser" to uidUser,
                "userName" to name
            )
            db.collection("room").document(idRoom).update("time", timeMessage)
            db.collection("room").document(idRoom).update("lastContent", lastContent)
            db.collection("room").document(idRoom).update("arrUserRead", ArrayList(arrUserRead))
        }
    }

    fun showOutGroupPopup(v: View) {
        ln_out_group.setBackgroundColor(Color.parseColor("#FFFD7129"))
        ln_member_ingroup.setBackgroundColor(Color.parseColor("#FF7F3D"))
        ln_add_member.setBackgroundColor(Color.parseColor("#FF7F3D"))

        val txtclose: TextView
        val btn_confirm: Button
        myDialog?.setContentView(R.layout.dialog_confirm_out_group)
        txtclose = myDialog!!.findViewById(R.id.txtclose)
        txtclose.text = "X"
        btn_confirm = myDialog!!.findViewById(R.id.btn_confirm) as Button
        txtclose.setOnClickListener { myDialog!!.dismiss() }
        btn_confirm.setOnClickListener {
            outGroup()
            myDialog!!.dismiss()
            finish()
        }
        myDialog!!.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog!!.show()
    }

    private fun outGroup() {
        val uid = FirebaseDatabase.getInstance().getReference("/messages").push().key.toString()
        val timeMessage = System.currentTimeMillis() / 1000
        db.collection("users").document(uidUser).update("arrRoom", FieldValue.arrayRemove(idRoom))
        db.collection("users").document(uidUser).get().addOnSuccessListener {
            val name = it["userName"] as String
            val message = hashMapOf(
                "content" to "$name đã rời khỏi nhóm",
                "time" to timeMessage,
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
                "content" to "$name đã rời khỏi nhóm",
                "fromUser" to uidUser,
                "userName" to name
            )
            db.collection("room").document(idRoom).update("uidMember", FieldValue.arrayRemove(uidUser))
            db.collection("room").document(idRoom).update("lastContent", lastContent)
            db.collection("room").document(idRoom).update("arrUserRead", ArrayList(arrUserRead))

        }

    }


    var selectedPhotoGroupuri: Uri? = null
    var selectedSentPhotouri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Chatting Group", "Photo was selected")
            selectedPhotoGroupuri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoGroupuri)
            val width = bitmap.getWidth()
            val height = bitmap.getHeight()
            var rate = 0.0f

            val maxResolution = 80

            var newWidth = width
            var newHeight = height

            if (width > height) {
                if (maxResolution < width) {
                    rate = maxResolution / (width.toFloat())
                    newHeight = (height * rate).toInt()
                    newWidth = maxResolution
                }

            } else {
                if (maxResolution < height) {
                    rate = maxResolution / height.toFloat()
                    newWidth = (width * rate).toInt()
                    newHeight = maxResolution
                }
            }

            val resizedImage = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            avatar_in_group.setImageBitmap(resizedImage)

            uploadImageToStorage()
        }
        //pick image to send
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val progessbar = progessbar(this)
            progessbar.show()

            Log.d("Image Group", "Photo was selected")
            selectedSentPhotouri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedSentPhotouri)
            val width = bitmap.getWidth()
            val height = bitmap.getHeight()
            var rate = 0.0f

            val maxResolution = 200

            var newWidth = width
            var newHeight = height

            if (width > height) {
                if (maxResolution < width) {
                    rate = maxResolution / (width.toFloat())
                    newHeight = (height * rate).toInt()
                    newWidth = maxResolution
                }

            } else {
                if (maxResolution < height) {
                    rate = maxResolution / height.toFloat()
                    newWidth = (width * rate).toInt()
                    newHeight = maxResolution
                }
            }
            val resizedImage = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            bitmapToFile(resizedImage)

            imageView_message_image.setImageBitmap(resizedImage)

            uploadSentImageToStorage(progessbar, newHeight, newWidth)
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference
    private fun uploadImageToStorage() {
        var imagesRef: StorageReference? = storageRef.child("images")
        if (selectedPhotoGroupuri == null) return
        else {
            val filename = UUID.randomUUID().toString()
            val ref = imagesRef?.child(filename)
            ref?.putFile(selectedPhotoGroupuri!!)?.addOnSuccessListener {
                Log.d("Chat", "Successfully upload image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Chat", "File location: $it")
                    saveUserToCloudFirestore(it.toString())
                }
            }?.addOnFailureListener {
                Log.d("Chat", "Failure")
            }
        }
    }

    private fun saveUserToCloudFirestore(imageUrl: String) {
        db.collection("room").document(idRoom).update("image", imageUrl)
            .addOnSuccessListener {
                Log.d("Chat", "save image into firestore successfully")
            }
    }

    private fun loadImageGroup(uid: String?) {
        db.collection("room").document(idRoom)
            .get()
            .addOnSuccessListener {
                room = it.toObject(Room::class.java)
                if (room?.image == "") return@addOnSuccessListener
                Picasso.get().load(room?.image).into(avatar_in_group)
                Log.d("load image", "success!")
            }
    }

//

    private fun uploadSentImageToStorage(progessbar: progessbar, newHeight: Int, newWdith: Int) {
        val imagesRef: StorageReference? = storageRef.child("images")
        if (selectedSentPhotouri == null) return
        else {
            val filename = UUID.randomUUID().toString()
            val ref = imagesRef?.child(filename)
            ref?.putFile(selectedSentPhotouri!!)?.addOnSuccessListener {
                Log.d("Send Image", "Successfully upload image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Send Image", "File location: $it")
                    toast("Gửi ảnh thành công")
                    saveMessageToDatabase("", it.toString(), newHeight, newWdith)
                    progessbar.dismiss()
                }
            }?.addOnFailureListener {
                Log.d("Send Image", "Failure")
                toast("Gửi thất bại!!")
                progessbar.dismiss()
            }
        }
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
