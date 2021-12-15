package com.example.wowCamera.utils

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException


class BitmapTools {
    companion object {
        fun getBitmapFromUriStream(context: Context, uri: Uri):Bitmap{
            val content = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(content)
            content?.close()
            return bitmap
        }

        fun rotate(b: Bitmap, degrees: Int): Bitmap {
            var b = b
            if (degrees != 0 && b != null) {
                val m = Matrix()
                m.setRotate(degrees.toFloat(), b.width.toFloat() / 2, b.height.toFloat() / 2)
                try {
                    val b2 = Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
                    if (b != b2) {
                        b.recycle() // Bitmap操作完应该显示的释放
                        b = b2
                    }
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                }
            }
            return b
        }
        fun toBitmap(image: Image): Bitmap { // 将Image转为Bitmap
            val yBuffer = image.planes[0].buffer // Y
            val uBuffer = image.planes[1].buffer // U
            val vBuffer = image.planes[2].buffer // V
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)
            //U and V are swapped
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        fun getPicFromBytes(bytes: ByteArray?, opts: BitmapFactory.Options?): Bitmap? {
            return if (bytes != null) if (opts != null) BitmapFactory.decodeByteArray(
                bytes, 0, bytes.size,
                opts
            ) else BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
        }

        fun flipHorizontal(bitmap: Bitmap): Bitmap? {
            val m = Matrix()
            m.setScale(-1F, 1F) // 水平翻转
            val w = bitmap.width
            val h = bitmap.height
            return Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true)
        }

        fun get9Image(bitmap: Bitmap) : ArrayList<Bitmap>{
            var bitmapList = ArrayList<Bitmap>()
            var width = bitmap.width
            var height = bitmap.height
            var proportion = 0.8
            for (i in 0..2){
                for (j in 0..2){
                    var bitmap_temp = Bitmap.createBitmap(
                        bitmap,
                        i * width / 10 * 2,
                        j * height / 10 * 2,
                        width / 10 * 6,
                        height / 10 * 6,
                        Matrix(),
                        true
                    )
                    bitmapList.add(bitmap_temp)
                }
            }
            return bitmapList
        }

        fun getExifOrientation(filepath: String?): Int {
            var degree = 0
            var exif: ExifInterface? = null
            try {
                exif = ExifInterface(filepath!!)
            } catch (ex: IOException) {
                Log.d("qxy", "cannot read exif$ex")
            }
            if (exif != null) {
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
                if (orientation != -1) {
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                    }
                }
            }
            return degree
        }

    }
}