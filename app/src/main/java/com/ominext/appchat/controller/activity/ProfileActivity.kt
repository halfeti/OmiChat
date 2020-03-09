package com.ominext.appchat.controller.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ominext.appchat.R
import com.quang.appchat.activity.MainActivity
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.act_profile.*
import kotlinx.android.synthetic.main.actionbarbottom_common.*
import java.util.*
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.lang.Double.parseDouble
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity : BaseActivity(), AdapterView.OnItemSelectedListener {


    var gender_list = arrayOf("Nam", "Nữ", "Khác")
    var spinner: Spinner? = null
    var db = FirebaseFirestore.getInstance()
    var user: User? = null
    var newBirthday: String = "1"
    var newPhoneNumber: String = "2"
    val DATE_PATTERN = "^(3[01]|[12][0-9]|[1-9]|0[1-9])(\\/|-)(0?[1-9]|1[012])(\\/|-)[0-9]{4}\$"
    val PHONENUMBER_PATERN = "[\\\\(]?[+]?(\\d*)[\\\\)]?[-\\s\\.]?(\\d*)[-\\s\\.]?(\\d*)[-\\s\\.]?(\\d*)[-\\s\\.]?(\\d*)"
    var selectedPhotouri: Uri? = null
    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference

    override fun setup() = Setup(
        MainActivity.TAG,
        R.layout.act_profile
    )


    override fun initView() {
        btn_profile.setBackgroundColor(Color.parseColor("#FFFD7129"))

        ln_home.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        ln_profile.setOnClickListener { ln_profile.hideKeyboard() }
        //Set username and email
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        loadAvatar(uid)
        readNameAndEmail(uid)

        //Gender Spinner
        spinner = this.gender_spinner
        spinner!!.setOnItemSelectedListener(this)
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, gender_list)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.setAdapter(aa)
        gender_spinner.isEnabled = false

        //Edit information
        getOtherInfo(uid)
        if (edit_profile.visibility == View.VISIBLE && edit_done_profile.visibility == View.GONE) {
            edit_profile.setOnClickListener {
                edit_profile.visibility = View.GONE
                edit_done_profile.visibility = View.VISIBLE
                edt_birthday.isEnabled = true
                edt_phone_number.isEnabled = true
                gender_spinner.isEnabled = true
                iv_pick_date.isEnabled = true
                newBirthday = edt_birthday.text.toString().trim()
                newPhoneNumber = edt_phone_number.text.toString().trim()
                iv_pick_date.setOnClickListener {
                    val c = Calendar.getInstance()
                    val year = c.get(Calendar.YEAR)
                    val month = c.get(Calendar.MONTH)
                    val day = c.get(Calendar.DAY_OF_MONTH)

                    val dpd = DatePickerDialog(
                        this,
                        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                            edt_birthday.text =
                                Editable.Factory.getInstance().newEditable("""$dayOfMonth/${monthOfYear + 1}/$year""")
                        },
                        year,
                        month,
                        day
                    )

                    dpd.show()
                }
                return@setOnClickListener
            }
        }
        edit_done_profile.setOnClickListener{
            edit_profile.visibility=View.VISIBLE
            edit_done_profile.visibility=View.GONE
            edt_birthday.isEnabled = false
            edt_phone_number.isEnabled = false
            gender_spinner.isEnabled = false
            iv_pick_date.isEnabled = false

            if(validInfo(edt_birthday.text.toString(), edt_phone_number.text.toString())) {
                edt_birthday.text = edt_birthday.text
                edt_phone_number.text = edt_phone_number.text
                edt_birthday.isEnabled = false
                edt_phone_number.isEnabled = false
                gender_spinner.setSelection(gender_spinner.selectedItemPosition)

                updateOtherInfo(uid)
                getOtherInfo(uid)
                toast("Thay đổi thành công")
                edt_birthday.hideKeyboard()
            }

        }

        //Click avatar action
        avatar.setOnClickListener {
            Log.d("ProfileActivity", "Try to show avatar photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        //Back to List Group
        btn_back_from_profile.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        btn_logout.setOnClickListener {
            status("off")
            clearpref("KEY_USER")
            FirebaseAuth.getInstance().signOut()
//            finish()
            var intent = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        initView()
    }

    private fun loadAvatar(uid: String?) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    user = it.toObject(User::class.java)
                    if (user!!.image == "" || user!!.image == null) return@addOnSuccessListener

                    Picasso.get().load(user?.image).into(avatar)
                }
        }
    }

    fun validateDate(date: String): Boolean {
        return DATE_PATTERN.toRegex().matches(date)
    }

    private fun readNameAndEmail(uid: String?) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    user = it.toObject(User::class.java)
                    username_profile.text = user?.userName
                    email_profile.text = user?.email
                }
        }
    }

    private fun updateOtherInfo(uid: String?) {
        if (uid != null) {
            val userUpdInfo = hashMapOf(
                "birthday" to edt_birthday.text.toString(),
                "phoneNumber" to edt_phone_number.text.toString(),
                "gender" to gender_spinner.selectedItem.toString()
            )
            db.collection("users").document(uid).update(userUpdInfo as Map<String, String>)
                .addOnSuccessListener {
                    Log.d("profile", "upd successfully")
                }
        }
    }

    private fun getOtherInfo(uid: String?) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    user = it.toObject(User::class.java)
                    edt_birthday.text = Editable.Factory.getInstance().newEditable(user?.birthday)
                    edt_phone_number.text = Editable.Factory.getInstance().newEditable(user?.phoneNumber)
                    setSpinText(gender_spinner, user?.gender.toString())
                    Log.d("profile", "read successfully")
                }
        }
    }

    fun setSpinText(spin: Spinner, text: String) {
        for (i in 0 until spin.adapter.count) {
            if (spin.adapter.getItem(i).toString().contains(text)) {
                spin.setSelection(i)
            }
        }
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Profile", "Photo was selected")
            selectedPhotouri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotouri)
            val width = bitmap.getWidth()
            val height = bitmap.getHeight()
            var rate = 0.0f

            val maxResolution = 100

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

            avatar.setImageBitmap(resizedImage)

            uploadImageToStorage()
        }
    }

    private fun uploadImageToStorage() {
        var imagesRef: StorageReference? = storageRef.child("images")
        if (selectedPhotouri == null) return
        else {
            val filename = UUID.randomUUID().toString()
            val ref = imagesRef?.child(
                filename
            )
            ref?.putFile(selectedPhotouri!!)?.addOnSuccessListener {
                Log.d("Profile", "Successfully upload image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Profile", "File location: $it")
                    saveUserToCloudFirestore(it.toString())
                }
            }?.addOnFailureListener {
                Log.d("Profile", "Failure")
            }
        }
    }

    private fun saveUserToCloudFirestore(profileImageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (uid != null) {

            db.collection("users").document(uid).update("image", profileImageUrl)
                .addOnSuccessListener {
                    Log.d("profile", "save image successfully")
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
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        db.collection("users").document(uid.toString()).update("status", stt)
    }

    fun convertString2Date(date: String): Date? {
        val arr = date.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in arr.indices) {
            if (Integer.parseInt(arr[i]) < 10) {
                arr[i] = "0" + arr[i]
            }
        }

        var tmpDate = arr.joinToString("/")
        val parser = SimpleDateFormat("dd/MM/yyyy")
        try {
            return parser.parse(tmpDate)
        } catch (e: ParseException) {
            return null
        }
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun checkAfterDate(date1: Date, date2: Date): Boolean {
        if (date1.compareTo(date2) > 0) {
            return true
        } else {
            return false
        }
    }

    fun validInfo(date: String, phoneNumber: String): Boolean {
        var res = true
        if (!validateDate(date)) {
            res = false
            toast("Ngày sinh sai định dạng")
        } else {
            var birthday = convertString2Date(edt_birthday.text.toString())
            var currentDate = getCurrentDateTime()
            if (birthday == null || (birthday != null && checkAfterDate(birthday, currentDate))) {
                res = false
                toast("Vui lòng nhập ngày nhỏ hơn ngày hiện tại")
            }
        }
        if (date.length > 10) {
            res = false
            toast("Nhập sai ngày sinh")
        }
        var numeric = true
        try {
            val num = parseDouble(edt_phone_number.text.toString())
        } catch (e: NumberFormatException) {
            numeric = false
        }
//        if (!numeric) toast("Chỉ nhập số")

        if (phoneNumber.length<8 || phoneNumber.length >20) {
            res = false
            toast("Số điện thoại phải từ 8 đến 20 số")
        }
        if (!validPhoneNumber(phoneNumber)) {
            res = false
            toast("Số điện thoại không hợp lệ")
        }
        return res
    }

    fun validPhoneNumber(phoneNumber: String): Boolean{
        return PHONENUMBER_PATERN.toRegex().matches(phoneNumber)
    }
}