package com.quang.appchat.controller.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.ominext.appchat.R
import com.quang.appchat.base.BaseActivity
import kotlinx.android.synthetic.main.act_add_member_to_group.*
import kotlinx.android.synthetic.main.act_insert_name_group.*
import kotlinx.android.synthetic.main.actionbar_member.*

class CreateNewGroupAct : BaseActivity() {


    override fun setup() = Setup(TAG, R.layout.act_insert_name_group)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_down, R.anim.fade_out)
        initView()
    }

    companion object {
        val TAG: String = CreateNewGroupAct::class.java.name
    }


    override fun initView() {
        text_create_group.text = "Tên Nhóm Của Bạn"
        btn_add_member.setOnClickListener {
            val nameGroup = edt_name_group.text.toString().trim()
            if (nameGroup.equals("")) {
                toast("Hãy đặt tên cho group của bạn")
                return@setOnClickListener
            }
            val i = Intent(this, AddMemberAct::class.java)
            i.putExtra("data", edt_name_group.text.toString())
            startActivityForResult(i, ListGroupAct.PICK_CONTACT_REQUEST)
            finish()
        }

        iv_back_member.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        ln_createNewGroup.setOnClickListener { ln_createNewGroup.hideKeyboard() }

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
        val db = FirebaseFirestore.getInstance()
        val uidUser = getPred("KEY_USER").toString()
        db.collection("users").document(uidUser).update("status", stt)
    }
}
