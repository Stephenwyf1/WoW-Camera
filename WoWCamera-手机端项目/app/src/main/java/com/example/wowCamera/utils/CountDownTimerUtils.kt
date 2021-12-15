package com.example.wowCamera.utils

import android.os.CountDownTimer
import android.widget.TextView

class CountDownTimerUtils(private val textView: TextView, millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
    override fun onTick(millisUntilFinished: Long) {
        textView.isClickable = false
        textView.text = (millisUntilFinished/1000).toString()+ "s后重新获取"
    }

    override fun onFinish() {
        textView.text = "重新获取"
        textView.isClickable = true
    }
}