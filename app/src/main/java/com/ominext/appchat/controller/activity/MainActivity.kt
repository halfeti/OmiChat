package com.quang.appchat.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.ominext.appchat.R
import com.quang.appchat.base.BaseActivity
import com.quang.appchat.controller.activity.ListGroupAct
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import kotlinx.android.synthetic.main.activity_main.*
import android.preference.PreferenceManager
import android.content.Context
import android.text.Editable
import android.R.id.edit
import android.app.Activity
import android.app.ActivityOptions
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.view.dialog.progessbar
import kotlinx.android.synthetic.main.act_register.*
import kotlinx.android.synthetic.main.act_start.*


class MainActivity : BaseActivity(), View.OnClickListener {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_goRegister -> {
                var i = Intent(this, RegisterAct::class.java)
                startActivity(i)
            }
            R.id.btn_login -> {
                signIn()
            }
        }
    }

    private fun signIn() {
        val progessbar = progessbar(this)
        progessbar.show()
        val edtEmail = edt_email_signin.text.toString().trim()
        val edtPassword = edt_password_signin.text.toString().trim()
        val preferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val preferences2 =getApplicationContext().getSharedPreferences("install_code_prefs", Activity.MODE_PRIVATE);
        val editor = preferences.edit()

        if (edtEmail.isEmpty()) {
            toast("Email không được để trống!!!")
            progessbar.dismiss()
            return
        }
        if (!edtEmail.validEmail()) {
            toast("Email không đúng định dạng!!!")
            progessbar.dismiss()

            return
        }
        if (edtPassword.isEmpty()){
            toast("Password không được để trống")
            progessbar.dismiss()

            return
        }
        Log.i(TAG, "Email: ${edtEmail}  + Passwor:  ${edtPassword}")



            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(edtEmail, edtPassword)
                    .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    //else if successfully
                    else{
                        val user = auth.currentUser
                        if (user==null) {
                            toast("Đăng nhập thất bại, vui lòng thử lại")
                            progessbar.dismiss()
                            return@addOnCompleteListener
                        }
                        //remember account
                        if (check_remember.visibility==View.VISIBLE && uncheck_remember.visibility==View.GONE){
                            editor.putString("username", edtEmail)
                            editor.putString("password", edtPassword)
                            toast("Đã ghi nhớ tài khoản")
                            progessbar.dismiss()
                            editor.commit()
                        }
                        if (!user?.isEmailVerified!!){
                            toast("Bạn chưa xác thực Email, hãy kiểm tra hộp thư của mình!")
                            progessbar.dismiss()
                            return@addOnCompleteListener
                        }
                        else{
                            savePref("KEY_USER",it.result.user.uid)
                            Log.i(TAG,"Key_user "+getPred("KEY_USER"))
                            val i = Intent(this, ListGroupAct::class.java)
                            startActivity(i)
                            finish()
                            toast("Đăng nhập thành công: $edtEmail")
                        }


                    }

                }
                .addOnFailureListener {
                    progessbar.dismiss()
                    toast("Đăng nhập thất bại :  ${it.message}")
                }


    }


    override fun initView() {
//        edt_email_signin.getBackground().clearColorFilter()
//        val preferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        val preferences2 =getApplicationContext().getSharedPreferences("install_code_prefs", Activity.MODE_PRIVATE);
//        val editor = preferences.edit()
//        if (preferences.getString("username", "")!=""){
//            check_remember.visibility=View.GONE
//            uncheck_remember.visibility=View.VISIBLE
//        }


        //click register
        tv_goRegister.setOnClickListener(this)
        btn_login.setOnClickListener(this)

        check_remember.setOnClickListener{
            check_remember.visibility=View.GONE
            uncheck_remember.visibility=View.VISIBLE
        }
        uncheck_remember.setOnClickListener{
            check_remember.visibility=View.VISIBLE
            uncheck_remember.visibility=View.GONE
        }

        forgot_your_password.setOnClickListener {
            val edtEmail = edt_email_signin.text.toString()
            if (edtEmail=="") toast("Nhập email vào Email")
            else{
                auth.sendPasswordResetEmail(edtEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("mail", "Email sent.")
                            toast("Hãy kiểm tra email email của bạn để đổi mật khẩu")
                        }
                    }
                    .addOnFailureListener {
                        toast("Nhập sai email")
                    }
            }

        }

    }

    override fun setup() = Setup(
        TAG,
        R.layout.activity_main
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getPred("KEY_USER") == null || getPred("KEY_USER") == ""){
            initView()
            val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val username = prefs.getString("username", "")
            val pwd = prefs.getString("password", "")
            edt_email_signin.text= Editable.Factory.getInstance().newEditable(username)
            edt_password_signin.text=Editable.Factory.getInstance().newEditable(pwd)
            ln_signIn.setOnClickListener { ln_signIn.hideKeyboard() }
        }else{
            val i = Intent(this, ListGroupAct::class.java)
            startActivity(i)
        }

    }

    companion object {
        val TAG: String = MainActivity::class.java.name
    }

    override fun onBackPressed() {
        finishAffinity()
    }

}
