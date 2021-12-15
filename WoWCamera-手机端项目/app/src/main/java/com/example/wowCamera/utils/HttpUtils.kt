package com.example.wowCamera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.Log
import com.example.wowCamera.utils.HttpUtils_bk.JSON
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread


object HttpUtils {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .pingInterval(3, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private const val baseUrl = "http://82.156.49.202"
        var newCall: Call? = null

    fun sendHttpRequest(address: String): ResponseBody? {
        val request = Request.Builder()
            .url(address)
            .build()
        this.newCall = client.newCall(request)
        val response = this.newCall!!.execute()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        this.newCall = null
        return response.body
    }

    fun sendConfig(json: JSONObject, mode: Int, listener: ConfigListener) {
        thread {
            Looper.prepare()
            try {
                var address = ""
                when (mode) {
                    1 -> { address = "/getOperation/" +  json.get("token") as String }
                    2 -> { address = "/saveOperation/" + json.get("token") as String + "/" + json.get("name") + "/" + json.get("operation") }
                    3 -> { address = "/deleteOperation/" + json.get("id") as String }
                    4 -> { address = "/updateOperation/" + json.get("id") as String + "/" + json.get("name")}
                }
                Log.d("TaG", "json:$json")
                val requestBody = json.toString().toRequestBody(JSON);
                val request = Request.Builder()
                    .url("$baseUrl:8080/op$address")
                    .post(requestBody)
                    .build()
                this.newCall = client.newCall(request)
                val response = this.newCall!!.execute()
                var info = response.body!!.string()
                listener.success(info)
            } catch (e: java.lang.Exception) {
                Log.d("config", "传输失败:$e")
                listener.error()
            } finally {
                this.newCall = null
            }
            Looper.loop()
        }
    }

    fun sendConfig(json: JSONObject, mode: Int, listener: (info: String) -> Unit) {
        thread {
            Looper.prepare()
            try {
                var address = ""
                when (mode) {
                    1 -> { address = "/getOperation/" +  json.get("token") as String }
                    2 -> { address = "/saveOperation/" + json.get("token") as String + "/" + json.get("name") + "/" + json.get("operation") }
                    3 -> { address = "/deleteOperation/" + json.get("id") as String }
                    4 -> { address = "/updateOperation/" + json.get("id") as String + "/" + json.get("name")}
                }
                Log.d("xdy", "json:$json")
                val requestBody = json.toString().toRequestBody(JSON);
                val request = Request.Builder()
                    .url("$baseUrl:8080/op$address")
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                Log.d("xdy", "address: $address")
                if(response.body != null) {
                    val info = response.body!!.string()
                    Log.d("xdy", "info: $info")
                    listener(info)
                } else {
                    listener("null")
                }
            } catch (e: java.lang.Exception) {
                Log.d("config", "传输失败:$e")
                listener("null")
            } finally {
                this.newCall = null
            }
            Looper.loop()
        }
    }

    fun sendImageForRequest(context: Context, url: String, bitmap: Bitmap,forImage:Boolean,listener: ImageListener) {
        thread {
            Looper.prepare()
            try {
                Log.d("xdy", "${baseUrl+url}")
                val file = getTempImageFile(context, bitmap)
                Log.d("xdy", "拿到文件")
                val imageRquest = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                var requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.name, imageRquest)
                    .build()
                val request = Request.Builder()
                    .url(baseUrl+url)
                    .post(requestBody)
                    .build()
                Log.d("xdy", "开始发送请求")
                this.newCall = client.newCall(request)
                Log.d("xdyTest", "sendImageForRequest: newCall: ${this.newCall}")
                val response = this.newCall!!.execute()
                Log.d("xdy", "responseCode: ${response.code}")
                if (response.code in 200..299 && response.body != null){
                    if (forImage){
                        val imageBytes = response.body!!.bytes()
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        listener.success(bitmap,null)
                    } else{
                        val poem = response.body!!.string()
                        listener.success(null, poem)
                    }
                }else{
                   listener.error(response.code)
                }
            } catch (e:Exception) {
                e.printStackTrace()
                listener.error(0)
            } finally {
                this.newCall = null
                Log.d("xdyTest", "sendImageForRequest: 结束 newCall: ${this.newCall}")
            }
        }
        Looper.loop()
    }

    fun sendStringForRequest(url: String,listener: ImageListener) {
        thread {
            Looper.prepare()
            try {
                val request = Request.Builder()
                    .url(baseUrl+url)
                    .get()
                    .build()
                Log.d("xdy", "开始发送请求")
                this.newCall = client.newCall(request)
                val response = this.newCall!!.execute()
                Log.d("xdy", "responseCode: ${response.code}")
                if (response.code in 200..299 && response.body != null){
                        val imageBytes = response.body!!.bytes()
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        listener.success(bitmap,null)
                }else{
                    listener.error(response.code)
                }

            }catch (e:Exception) {
                e.printStackTrace()
                listener.error(0)
            }finally {
                this.newCall = null
            }
        }
        Looper.loop()
    }

    private fun getTempImageFile(context: Context, bitmap: Bitmap): File {
        val tempPath = context.cacheDir.absolutePath + "/" + System.currentTimeMillis() + ".jpeg"
        val file = File(tempPath)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.run {
            flush()
            close()
        }
        return file
    }
}




