package com.ominext.appchat.controller.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.ominext.appchat.R
import com.quang.appchat.activity.MainActivity
import kotlinx.android.synthetic.main.act_start.*

class StartActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_start)

        val cowndown = object : CountDownTimer(1750, 10) {
            override fun onTick(millisUntilFinished: Long) {
                var current = progressBar.progress
                if (current>=progressBar.max){
                    current=0
                }
                progressBar.setProgress(current + 3)
            }

            override fun onFinish() {
                directToLogin()
            }
        }
        cowndown.start()


    }

    private fun directToLogin(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}