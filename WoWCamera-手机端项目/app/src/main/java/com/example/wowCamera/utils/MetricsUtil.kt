package com.example.wowCamera.utils

import android.graphics.Bitmap
import android.util.Log
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

object MetricsUtil{
    fun getSNR(bitmap: Bitmap): Float {
        var sum = 0L
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        Log.d("xdy", "width: $width")
        Log.d("xdy", "height: $height")
        Log.d("xdy", "size: ${pixels.size}")
        for (i in pixels.indices) {
            val clr = pixels[i]
            val red = clr and 0x00ff0000 shr 16 // 取高两位
            val green = clr and 0x0000ff00 shr 8 // 取中两位
            val blue = clr and 0x000000ff // 取低两位
            sum += (red + green + blue)
        }
        Log.d("xdy", "$sum")
        val avg = sum.toDouble() / pixels.size / 3
        Log.d("xdy", "$avg")
        var sd_sum = 0.0
        for (i in pixels.indices) {
            val clr = pixels[i]
            val red = clr and 0x00ff0000 shr 16 // 取高两位
            val green = clr and 0x0000ff00 shr 8 // 取中两位
            val blue = clr and 0x000000ff // 取低两位
            sd_sum += (red - avg).pow(2)
            sd_sum += (green - avg).pow(2)
            sd_sum += (blue - avg).pow(2)
        }
        val sd = sqrt(sd_sum / pixels.size / 3)
        Log.d("xdy", "sd: $sd")
        return sigmoid((1F / sd).toFloat())
    }

    fun getSMD2(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        val sizes = pixels.size
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var sum = 0L
        for (i in pixels.indices) {
            if (i >= 1 && i < sizes - width) {
                val p1 = pixels[i]
                val red1 = p1 and 0x00ff0000 shr 16
                val green1 = p1 and 0x0000ff00 shr 8
                val blue1 = p1 and 0x000000ff
                val p2 = pixels[i - 1]
                val red2 = p2 and 0x00ff0000 shr 16
                val green2 = p2 and 0x0000ff00 shr 8
                val blue2 = p2 and 0x000000ff
                val p3 = pixels[i + width]
                val red3 = p3 and 0x00ff0000 shr 16
                val green3 = p3 and 0x0000ff00 shr 8
                val blue3 = p3 and 0x000000ff
                val v1 = abs(red1 - red2) / 3 * abs(red1 - red3) / 3
                val v2 = abs(green1 - green2) / 3 * abs(green1 - green3) / 3
                val v3 = abs(blue1 - blue2) / 3 * abs(blue1 - blue3) / 3
                sum += (v1+v2+v3)
            }
        }
        Log.d("xdy", "sum: ${sum / sizes}")
    return sigmoid((sum / sizes).toFloat())
    }

    fun sigmoid(input: Float): Float {
        return 1 / (1 + exp(-1 * input))
    }
}