package com.example.wowCamera.utils

interface AnalyseListener {
    fun onScoreFinish(data: ArrayList<Float>)
    fun onScoreFail()
    fun onKeyPointFinish(data: ArrayList<KeyPoint>)
}