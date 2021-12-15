package com.example.wowCamera.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wowCamera.R
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val model = AiBoostPoseNet(this)
        model.init()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ai)
        val scaleBitmap = Bitmap.createScaledBitmap(bitmap, 257, 257, true).copy(Bitmap.Config.ARGB_8888, true)
        val outBitmap = model.process(scaleBitmap)
        test_iv.setImageBitmap(outBitmap)
    }
}