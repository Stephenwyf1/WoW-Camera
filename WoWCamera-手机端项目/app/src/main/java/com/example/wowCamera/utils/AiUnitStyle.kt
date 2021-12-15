package com.example.wowCamera.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aiunit.vision.common.ConnectionCallback
import com.aiunit.vision.common.FrameInputSlot
import com.aiunit.vision.common.FrameOutputSlot
import com.coloros.ocs.ai.cv.CVUnit
import com.coloros.ocs.ai.cv.CVUnitClient


class AiUnitStyle(context: Context) {
    lateinit var mCVClient: CVUnitClient
    val context = context
    var startCode = 1
    fun process(bitmap: Bitmap, listener: (data: Bitmap) -> Unit) {
        mCVClient = CVUnit.getVideoStyleTransferDetectorClient(context).addOnConnectionSucceedListener {
                Log.i("xdy", " authorize connect: onConnectionSucceed")
            }.addOnConnectionFailedListener { connectionResult ->
                Log.e("xdy", " authorize connect: onFailure: " + connectionResult.errorCode)
            }

        mCVClient.initService(context, object : ConnectionCallback {
            override fun onServiceConnect() {
                Log.i("xdy", "initService: onServiceConnect")
                startCode = mCVClient.start()
                Log.d("xdy", startCode.toString())
                if (startCode==0) {
                    var output = run(bitmap)
                    listener(output)
                }
            }

            override fun onServiceDisconnect() {
                Log.e("xdy", "initService: onServiceDisconnect: ")
            }
        })

    }

    fun run(bitmap: Bitmap): Bitmap {
        val inputSlot = mCVClient.createInputSlot() as FrameInputSlot
        inputSlot.targetBitmap = bitmap
        val outputSlot = mCVClient.createOutputSlot() as FrameOutputSlot
        mCVClient.process(inputSlot, outputSlot)
        val outFrame = outputSlot.outFrameData
        val outImageBuffer: ByteArray = outFrame.data
        Log.d("xdy", outFrame.width.toString() + " " + outFrame.height.toString() + " " + outFrame.data.size)

        val arrayOfInt = IntArray(outImageBuffer.size / 3)
        for (b in 0 until outFrame.height) {
            for (b1 in 0 until outFrame.width) {
                val b2: Byte = outImageBuffer[(outFrame.width * b + b1) * 3]
                val b3: Byte = outImageBuffer[(outFrame.width * b + b1) * 3 + 1]
                val b4: Byte = outImageBuffer[(outFrame.width * b + b1) * 3 + 2]
                arrayOfInt[outFrame.width * b + b1] = 255 shl 24 or (b2.toInt() shl 16) or (b3.toInt() shl 8) or b4.toInt()
            }
        }
        return Bitmap.createBitmap(arrayOfInt, outFrame.width, outFrame.height, Bitmap.Config.ARGB_8888);
    }

    fun close() {
        mCVClient.stop()
        mCVClient.releaseService();
    }
}