package com.example.wowCamera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.wowCamera.ml.PosenetModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

class PoseNet(context: Context) {
    var model: PosenetModel = PosenetModel.newInstance(context)
    private val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 257, 257, 3), DataType.FLOAT32)

    fun process(bitmap: Bitmap): ArrayList<KeyPoint> {
        val scaleBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val inputBuffer = initInputArray(scaleBitmap)
        inputFeature0.loadBuffer(inputBuffer)
        val outputs = model.process(inputFeature0)
        val heatMap = outputs.outputFeature0AsTensorBuffer.floatArray
        val offset = outputs.outputFeature1AsTensorBuffer.floatArray
        return getKeyPointList(heatMap, offset)
    }

    private fun getKeyPointList(
        heatMap: FloatArray,
        offsets: FloatArray,
    ): ArrayList<KeyPoint> {
        val keyPointList = ArrayList<KeyPoint>()
        for (i in 0 until pointsNum) {
            var maxValue = -1e10F
            var row = 0
            var col = 0
            for (y in 0 until blockHeight) {
                for (x in 0 until blockWidth) {
                    val value = heatMap[y * 9 * 17 + x * 17 + i]
                    if (value > maxValue) {
                        maxValue = value
                        row = y
                        col = x
                    }
                }
            }
            val keyPoint = KeyPoint(row, col, i)
            keyPoint.conf = sigmoid(maxValue)
            keyPoint.y =
                (row * 256 / 8 + offsets[row * 9 * 34 + col * 34 + i])
            keyPoint.x =
                (col * 256 / 8 + offsets[row * 9 * 34 + col * 34 + i + 17])
            keyPointList.add(keyPoint)
        }
        return keyPointList
    }

    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    // 输入数据初始化
    private fun initInputArray(bitmap: Bitmap): ByteBuffer {
        val bytesPerChannel = 4
        val inputChannels = 3
        val batchSize = 1
        val inputBuffer = ByteBuffer.allocateDirect(
            batchSize * bytesPerChannel * bitmap.height * bitmap.width * inputChannels
        )
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()
        val mean = 128.0f
        val std = 128.0f
        val intValues = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixelValue in intValues) {
            inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - mean) / std)
            inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - mean) / std)
            inputBuffer.putFloat(((pixelValue and 0xFF) - mean) / std)
        }
        return inputBuffer
    }

    fun closeModel() {
        model.close()
    }

    companion object {
        const val inputWidth = 257
        const val inputHeight = 257
        const val pointsNum = 17
        const val threshold = 0.5
        const val blockWidth = 9
        const val blockHeight = 9
        val keyPointGroups = arrayOf(
            arrayOf(0, 1),
            arrayOf(0, 2),
            arrayOf(1, 3),
            arrayOf(2, 4),
            arrayOf(5, 6),
            arrayOf(5, 7),
            arrayOf(6, 8),
            arrayOf(7, 9),
            arrayOf(8, 10),
            arrayOf(5, 11),
            arrayOf(6, 12),
            arrayOf(11, 12),
            arrayOf(11, 13),
            arrayOf(12, 14),
            arrayOf(13, 15),
            arrayOf(12, 14),
            arrayOf(14, 16)
        )
        // 注意左右是反的
        val keyPointNameForFront = arrayOf(
            "头部",
            "头部",
            "头部",
            "头部",
            "头部",
            "右肩",
            "左肩",
            "右肘",
            "左肘",
            "右腕",
            "左腕",
            "右胯",
            "左胯",
            "右膝盖",
            "左膝盖",
            "右脚踝",
            "左脚踝"
        )
    }
}