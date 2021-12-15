package com.example.wowCamera.utils

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object JsonUtils {
    fun getJsonData(fileName: String, context: Context): ArrayList<HashMap<Int, PoseKeyPoint>> {
        val stringBuilder = StringBuilder()
        try {
            val assetManager = context.assets
            val bf = BufferedReader(
                InputStreamReader(
                    assetManager.open(fileName!!)
                )
            )
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val jsonString =  stringBuilder.toString()
        val jsonArray = JSONArray(jsonString)
        return getStandardPoseList(jsonArray)
    }

    private fun getStandardPoseList(jsonArray: JSONArray): ArrayList<HashMap<Int, PoseKeyPoint>> {
        val poseArray = ArrayList<HashMap<Int, PoseKeyPoint>>()
        for(i in 0 until jsonArray.length()) {
            val poseMap = HashMap<Int, PoseKeyPoint>()
            val pose = jsonArray.getJSONArray(i)
            for(j in 0 until pose.length()) {
                val keyPoint = pose.getJSONObject(j)
                val index = keyPoint.getInt("index")
                val x = keyPoint.getDouble("x")
                val y = keyPoint.getDouble("y")
                poseMap[index] = PoseKeyPoint(x.toFloat(), y.toFloat())
            }
            poseArray.add(poseMap)
        }
        return poseArray
    }
}