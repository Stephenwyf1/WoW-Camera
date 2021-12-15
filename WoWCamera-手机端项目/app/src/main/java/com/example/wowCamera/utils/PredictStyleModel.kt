package com.example.wowCamera.utils

import android.content.Context
import android.graphics.Bitmap
import com.example.wowCamera.ml.PredictionModel
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer

class PredictStyleModel(context: Context) {
    private val model = PredictionModel.newInstance(context)

    fun getStyleBottleneck(styleBitmap: Bitmap): ByteBuffer {
        val styleBitmap = Bitmap.createScaledBitmap(styleBitmap, 256, 256, false)
        val styleImage = TensorImage.fromBitmap(styleBitmap)
        val outputs = model.process(styleImage)
        return outputs.styleBottleneckAsTensorBuffer.buffer
    }

    fun close() {
        model.close()
    }
}