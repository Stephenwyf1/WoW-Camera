package com.example.wowCamera

import android.app.ActionBar
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.contentValuesOf
import com.example.wowCamera.adapters.OrdersPagerAdapter
import com.example.wowCamera.utils.AiUnitAes
import com.example.wowCamera.utils.HttpUtils
import com.example.wowCamera.utils.MyDatabaseHelper
import kotlinx.android.synthetic.main.activity_process.*
import kotlinx.android.synthetic.main.base_loading.view.*
import kotlinx.android.synthetic.main.poem_popup_window.view.*
import kotlinx.android.synthetic.main.popup_window.view.*
import okhttp3.Call
import org.tensorflow.lite.support.metadata.schema.Content
import java.io.File
import kotlin.math.log

open class BaseActivity : AppCompatActivity() {
    private val TAG = "xdyTest"
    private lateinit var alertDialog: AlertDialog
    private var isShowing = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun messagevoid(type:Int) {
        Log.d("xdy", "messagevoid: 开始")
        object : Thread() {
            override fun run() {
                Looper.prepare()
                showLoadingDialog(type)
                Looper.loop()
            }
        }.start()
    }
//    type: 1-默认；2-作诗；3-去噪
     fun showLoadingDialog(type:Int) {
        if (!isShowing) {
            Log.d("xdy", "showloading: 开始")
            alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setCancelable(false)
            alertDialog.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK }
            alertDialog.show()
            val alertView = LayoutInflater.from(this).inflate(R.layout.base_loading, null)

            alertDialog.setContentView(alertView)
            when(type){
                1 -> alertView.words.text ="正在加载中..."
                2 -> alertView.words.text ="AI作诗时间较长，请耐心等待"
                3 -> alertView.words.text ="正在去噪中..."
                else->alertView.words.text ="正在加载中..."
            }
            alertDialog.setCanceledOnTouchOutside(true)
            isShowing = true
            alertDialog.setOnCancelListener {
                Log.d(TAG, "showLoadingDialog: 触发取消事件 ${HttpUtils?.newCall}")
                HttpUtils?.newCall?.cancel()
            }
        }
    }

    fun dismissLoadingDialog() {
        if(isShowing) {
            if(alertDialog != null) {
                alertDialog.dismiss()
                isShowing = false
            }
        }
    }
//    type:1-AI作诗，2-模板分享
    fun showPopWindow(string: String,type:Int,layout: View) {
        val pview = LayoutInflater.from(this).inflate(R.layout.poem_popup_window, null)
        val width = layout.width/10*9
        val popupWindow = PopupWindow(
            pview,
            width,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        when(type){
            1 -> pview.title_tv.text = "AI作诗"
            2 -> pview.title_tv.text = "模板分享"
            else -> pview.title_tv.text = " "
        }
        popupWindow.isTouchable = true
        popupWindow.overlapAnchor = true
        pview.poem_edit.text = SpannableStringBuilder(string)
        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0)
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        val inputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS) //这里给它设置了弹出的时间

        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0)
        pview.close_btn.setOnClickListener {
            popupWindow.dismiss()
        }
        pview.copy_btn.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            Log.d("xdy", "test"+pview.poem_edit.text.toString())
            val finalPoem = pview.poem_edit.text.toString()
            val mClipData = ClipData.newPlainText("Poem", finalPoem)
            cm.setPrimaryClip(mClipData)
            Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
        }
    }

    fun getPoint(aesModel: AiUnitAes, tempBitmap: Bitmap): String {
        var score = aesModel.process(tempBitmap)
        var scoreStr = String.format("%.2f", (70 * score - 50) / 3)
        if (scoreStr.equals("-40.00")){
            scoreStr = "请打开AIUnit"
        }
        return scoreStr
    }

    fun changePoint(point: Double): Double {
        val scoreNew = (70 * point - 50) / 3
        return if(scoreNew < 100) {
            scoreNew
        } else {
            100.0
        }
    }

    fun addPhotosql(dbHelper: MyDatabaseHelper, imgurl: String, score: String, fotime: String){
        val db = dbHelper.writableDatabase
        contentValuesOf("" to "")
        val values = ContentValues().apply {
            put("imgurl", imgurl)
            put("score", score)
            put("fotime", fotime)
        }
        db.insert("Album", null, values) // 插入数据

    }

}