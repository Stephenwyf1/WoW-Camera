package com.example.wowCamera.utils

import android.content.Context
import android.graphics.Bitmap
import com.example.wowCamera.ml.TransferModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


class TransferStyleModel(context: Context) {
    private val model = TransferModel.newInstance(context)

    fun getTransformImage(bottleneck1: ByteBuffer, bottleneck2: ByteBuffer, originBitmap: Bitmap): Bitmap {
        val width = originBitmap.width
        val height = originBitmap.height
        val originBitmap = Bitmap.createScaledBitmap(originBitmap, 384, 384, false)
        val contentImage = TensorImage.fromBitmap(originBitmap)
        val styleBottleneck = TensorBuffer.createFixedSize(
            intArrayOf(1, 1, 1, 100),
            DataType.FLOAT32
        )



        styleBottleneck.loadBuffer(mixStyle(bottleneck1, bottleneck2))
        val outputs = model.process(contentImage, styleBottleneck)
        val styledImage = outputs.styledImageAsTensorImage
        return Bitmap.createScaledBitmap(styledImage.bitmap, width, height, false)
    }

    fun mixStyle(bottleneck1: ByteBuffer, bottleneck2: ByteBuffer): ByteBuffer {
        bottleneck1.position(0)
        bottleneck2.position(0)

        val byteBuffer = ByteBuffer.allocate(400)
        byteBuffer.order(ByteOrder.nativeOrder())

        for(i in 0 until 100){
            val f1 = bottleneck1.float
            val f2 = bottleneck2.float
            byteBuffer.putFloat((f1+f2)/2)
        }
        return byteBuffer
    }

    fun close() {
        model.close()
    }
}