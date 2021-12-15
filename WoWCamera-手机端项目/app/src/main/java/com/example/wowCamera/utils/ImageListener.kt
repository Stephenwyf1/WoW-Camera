package com.example.wowCamera.utils

import android.graphics.Bitmap
interface ImageListener {
    fun success(bitmap: Bitmap?,string: String?)
    fun error(code: Int)
}