package com.quang.appchat.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.ominext.appchat.view.dialog.progessbar
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.model.User
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.act_register.*

class RegisterAct : BaseActivity() {

    var db = FirebaseFirestore.getInstance()
    private val idRoom = ArrayList<String>()

    override fun initView() {
        btn_register.setOnClickListener {
            performRegister()
        }
        btn_goTologin.setOnClickListener {
            finish()
        }
        ln_register.setOnClickListener {
            ln_register.hideKeyboard()
        }
    }

    private fun performRegister() {
        val progessbar = progessbar(this)
        progessbar.show()
        val edtEmail = edt_email.text.toString().trim()
        val edtPassword = edt_password.text.toString().trim()
        val edtConfirmPassword = edt_password_confirm.text.toString().trim()
        val edtUserName = edt_userName.text.toString().trim()

        //check username
        if (edtUserName.isEmpty()){
            toast("Tên người dùng không được để trống!!")
            alert_regis_user.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_user.visibility=View.INVISIBLE

        if (edtUserName.length>30){
            toast("Tên người dùng không quá 30 ký tự!!")
            alert_regis_user.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_user.visibility=View.INVISIBLE

        //check email
        if (edtEmail.isEmpty() ) {
            toast("Email không được để trống !!!")
            alert_regis_email.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_email.visibility=View.INVISIBLE

        if (!edtEmail.validEmail()) {
            toast("Email không đúng định dạng !!!")
            alert_regis_email.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_email.visibility=View.INVISIBLE

        if (edtEmail.length>32) {
            toast("Email không quá 32 ký tự !!!")
            alert_regis_email.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_email.visibility=View.INVISIBLE

        //check password
        if (edtPassword.isEmpty() ) {
            toast("Mật khẩu không được để trống !!!")
            alert_regis_password.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_password.visibility=View.INVISIBLE
        if (edtPassword.length > 64 || edtPassword.length <8 || edtPassword.isEmpty()){
            toast("Password phải từ 8 đến 64 ký tự")
            alert_regis_password.visibility=View.VISIBLE
            progessbar.dismiss()
            return
        } else alert_regis_password.visibility=View.INVISIBLE

        if (edtConfirmPassword != edtPassword){
            alert_regis_confirm_password.visibility=View.VISIBLE
            toast("Mật khẩu không trùng khớp.")
            progessbar.dismiss()
            return
        } else alert_regis_confirm_password.visibility=View.INVISIBLE

        // verify email
        val auth = FirebaseAuth.getInstance()

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(edtEmail, edtPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = auth.currentUser
                        if (user==null){
                            toast("Đăng kí thất bại, vui lòng thử lại")
                            progessbar.dismiss()
                            return@addOnCompleteListener
                        }
                        alert_regis_email.visibility=View.INVISIBLE
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Email sent.")
                                    saveUserToDatabase(progessbar)
                                    toast("Đăng ký thành công  $edtEmail, vui lòng xác thực email")
                                    progessbar.dismiss()

                                    finish()
                                }
                            }
                            ?.addOnFailureListener {
                                toast("Lỗi gửi email xác thực, bạn vui lòng đăng kí lại")
                                progessbar.dismiss()

                                return@addOnFailureListener
                            }
                    }
                    else if (!it.isSuccessful){
                        alert_regis_email.visibility=View.VISIBLE
                        progessbar.dismiss()

                        return@addOnCompleteListener
                    }
                }
                .addOnFailureListener {
                    progessbar.dismiss()

                    toast("Đăng ký thất bại :  ${it.message}")
                }


    }

    fun View.toggleVisibility() {
        if (visibility == View.VISIBLE) {
            visibility = View.INVISIBLE
        } else {
            visibility = View.VISIBLE
        }
    }



    private fun saveUserToDatabase(progessbar: progessbar ) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val user = hashMapOf(
            "uid" to uid,
            "userName" to edt_userName.text.toString(),
            "email" to edt_email.text.toString(),
            "password" to edt_password.text.toString(),
            "image" to "https://firebasestorage.googleapis.com/v0/b/appchat-e2938.appspot.com/o/images%2Fa76dfd54-a2b4-4de3-909c-97de1cd5e893?alt=media&token=a13647bc-cdfe-4634-864e-461f81d9f032",
            "check" to "0",
            "birthday" to "",
            "gender" to "",
            "phoneNumber" to "",
            "status" to "off",
            "arrRoom" to ArrayList(idRoom)
        )
        db.collection("users").document(uid)
            .set(user as Map<String, Any>)
            .addOnSuccessListener {
                progessbar.dismiss()
                Log.i(TAG, "DocumentSnapshot added with ID: ${it}")
            }
            .addOnFailureListener { e ->
                toast("Error adding document: $e")
                progessbar.dismiss()
                Log.i(TAG, "Error adding document", e)
            }

    }

    override fun setup() = Setup(TAG, R.layout.act_register)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

    }

    companion object {
        private val TAG: String = RegisterAct::class.java.name
    }

}