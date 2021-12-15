package com.example.wowCamera.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.aiunit.vision.common.ConnectionCallback
import com.aiunit.vision.common.FrameInputSlot
import com.aiunit.vision.common.FrameOutputSlot
import com.aiunit.vision.picture.SkyReplaceInputSlot
import com.coloros.ocs.ai.cv.CVUnit
import com.coloros.ocs.ai.cv.CVUnitClient
import com.example.wowCamera.R
import java.nio.ByteBuffer


class AiUnitSky(context: Context) {
    lateinit var mCVClient: CVUnitClient
    val context = context
    var startCode = 1
    var skyBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sky)
    fun process(bitmap: Bitmap, listener: (data: Bitmap) -> Unit) {
        mCVClient = CVUnit.getSkyReplaceDetectorClient(context).addOnConnectionSucceedListener {
            Log.i("TAG", " authorize connect: onConnectionSucceed")
        }.addOnConnectionFailedListener { connectionResult -> Log.e("TAG", " authorize connect: onFailure: " + connectionResult.errorCode)
        }
        mCVClient.initService(context, object : ConnectionCallback {
            override fun onServiceConnect() {
                Log.i("TAG", "initService: onServiceConnect")
                startCode = mCVClient.start()
                if (startCode==0) {
                    var output = run(bitmap)
                    listener(output)
                }
            }
            override fun onServiceDisconnect() {
                Log.e("TAG", "initService: onServiceDisconnect: ")
            }
        })
    }

    fun run(bitmap: Bitmap): Bitmap {
        val inputSlot = mCVClient.createInputSlot() as FrameInputSlot
        if (inputSlot != null) {
            (inputSlot as SkyReplaceInputSlot).skyBitmap = skyBitmap
        }
        inputSlot.targetBitmap = bitmap
        val outputSlot = mCVClient.createOutputSlot() as FrameOutputSlot
        mCVClient.process(inputSlot, outputSlot)
        val outFrame = outputSlot.outFrameData
        val outImageBuffer = outFrame.data
        val stitchBmp = Bitmap.createBitmap(outFrame.width, outFrame.height, Bitmap.Config.ARGB_8888)

        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(outImageBuffer))

        return stitchBmp
    }

    fun close() {
        mCVClient.stop()
        mCVClient.releaseService();
    }
}