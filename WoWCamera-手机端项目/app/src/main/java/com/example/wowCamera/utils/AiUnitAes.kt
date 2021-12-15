package com.example.wowCamera.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aiunit.vision.common.ConnectionCallback
import com.aiunit.vision.common.FrameInputSlot
import com.aiunit.vision.picture.AestheticScoreOutputSlot
import com.coloros.ocs.ai.cv.CVUnit
import com.coloros.ocs.ai.cv.CVUnitClient

class AiUnitAes(context: Context) {
    lateinit var mCVClient: CVUnitClient
    val context = context
    var startCode = 1
    fun connect() {
        mCVClient = CVUnit.getAestheticsClient(context).addOnConnectionSucceedListener {
            Log.i("TAG", "authorize connect: onConnectionSucceed")
        }.addOnConnectionFailedListener { connectionResult ->
            Log.e("TAG", "authorize connect: onFailure: " + connectionResult.errorCode)
        }
        mCVClient.initService(context, object : ConnectionCallback {
            override fun onServiceConnect() {
                Log.i("TAG", "initService: onServiceConnect")
                startCode = mCVClient.start()

            }
            override fun onServiceDisconnect() {
                Log.e("TAG", "initService: onServiceDisconnect: ")
            }
        })
    }

    fun process(bitmap: Bitmap) : Float{
        return if(startCode==0){
            val inputSlot = mCVClient.createInputSlot() as FrameInputSlot
            inputSlot.targetBitmap = bitmap
            val outputSlot = mCVClient.createOutputSlot() as AestheticScoreOutputSlot
            mCVClient.process(inputSlot, outputSlot)
            outputSlot.score
        }else{
            -1F
        }
    }

    fun close() {
        if(startCode==0){
            startCode=1
            mCVClient.stop()
            mCVClient.releaseService();
        }
    }
}