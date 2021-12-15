package com.example.wowCamera.utils
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.ai.aiboost.AiBoostInterpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp


class AiBoostPoseNet(val context: Context) {
    var options: AiBoostInterpreter.Options? = null
    var aiboost: AiBoostInterpreter? = null

    fun init() {
        options = AiBoostInterpreter.Options()
        options!!.setNumThreads(1)
        options!!.setDeviceType(AiBoostInterpreter.Device.GPU)
        options!!.setNativeLibPath(context.applicationInfo.nativeLibraryDir)
        val input = context.assets.open("posenet_model.tflite")
        val length = input.available()
        val buffer = ByteArray(length)
        input.read(buffer)
        val modelbuf = ByteBuffer.allocateDirect(length)
        modelbuf.order(ByteOrder.nativeOrder())
        modelbuf.put(buffer)
        aiboost = AiBoostInterpreter(modelbuf, input_shapes, options)
    }

    fun process(bitmap: Bitmap): Bitmap {
        val imgData = aiboost!!.getInputTensor(0)
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until IMAGE_SIZE_X) {
            for (j in 0 until IMAGE_SIZE_Y) {
                val value: Int = intValues[pixel++]
                imgData.put((value shr 16 and 0xFF).toByte())
                imgData.put((value shr 8 and 0xFF).toByte())
                imgData.put((value and 0xFF).toByte())
            }
        }
        val output1 = aiboost!!.getOutputTensor(0)
        val output2 = aiboost!!.getOutputTensor(1)

        aiboost!!.runWithOutInputOutput()

        val floatBuff1 = output1.asFloatBuffer()
        val floatBuff2 = output2.asFloatBuffer()

        val result1 = FloatArray(floatBuff1.remaining())
        val result2 = FloatArray(floatBuff2.remaining())

        val kl = getKeyPointList(result1, result2, bitmap)
        return getWholeImage(bitmap, kl)

    }

    private fun getKeyPointList(
        heatMap: FloatArray,
        offsets: FloatArray,
        bitmap: Bitmap
    ): ArrayList<KeyPoint> {
        val originWidth = bitmap.width
        val originHeight = bitmap.height
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
            Log.d(TAG, maxValue.toString())

            val keyPoint = KeyPoint(row, col, i)
            keyPoint.conf = sigmoid(maxValue)
            keyPoint.y =
                (row * 256 / 8 + offsets[row * 9 * 34 + col * 34 + i]) * originHeight / inputHeight
            keyPoint.x =
                (col * 256 / 8 + offsets[row * 9 * 34 + col * 34 + i + 17]) * originWidth / inputWidth
            keyPointList.add(keyPoint)
        }
        return keyPointList
    }

    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    private fun getWholeImage(bitmap: Bitmap, keyPointList: ArrayList<KeyPoint>): Bitmap {
        val outBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val originWidth = outBitmap.width
        val canvas = Canvas(outBitmap)
        val paint = Paint()
        val strokeSize = originWidth.toFloat() / 100
        paint.strokeWidth = strokeSize
        paint.color = Color.rgb(0x4F, 0xC3, 0xF7)
        for (keyPointGroup in keyPointGroups) {
            val keyPoint0 = keyPointList[keyPointGroup[0]]
            val keyPoint1 = keyPointList[keyPointGroup[1]]
            if (keyPoint0.conf > threshold && keyPoint1.conf > threshold)
                canvas.drawLine(
                    keyPoint0.x,
                    keyPoint0.y,
                    keyPoint1.x,
                    keyPoint1.y,
                    paint
                )
        }
        paint.color = Color.rgb(0xFF, 0x8A, 0x65)
        for (keyPoint in keyPointList) {
            Log.d(TAG, "x: ${keyPoint.x} y: ${keyPoint.y}")
            if (keyPoint.conf > threshold) {
                canvas.drawCircle(
                    keyPoint.x,
                    keyPoint.y,
                    strokeSize,
                    paint
                )
            }
        }
        return outBitmap
    }

    companion object {
        const val TAG = "xdyTest"
        const val pointsNum = 17
        const val inputWidth = 257
        const val inputHeight = 257
        const val blockWidth = 9
        const val blockHeight = 9
        private const val DIM_BATCH_SIZE = 1
        private const val DIM_PIXEL_SIZE = 3
        private const val IMAGE_SIZE_X = 257
        private const val IMAGE_SIZE_Y = 257
        val intValues = IntArray(IMAGE_SIZE_X * IMAGE_SIZE_Y)
        val input_shapes =
            arrayOf(intArrayOf(DIM_BATCH_SIZE, IMAGE_SIZE_Y, IMAGE_SIZE_X, DIM_PIXEL_SIZE))
        const val threshold = 0
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
    }

}