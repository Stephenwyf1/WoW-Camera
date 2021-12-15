package com.example.wowCamera

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wowCamera.CameraActivity.Companion.FILENAME_FORMAT
import com.example.wowCamera.adapters.ConfigStyle
import com.example.wowCamera.adapters.DialogAdapter
import com.example.wowCamera.adapters.pattern
import com.example.wowCamera.adapters.patternAdapter
import com.example.wowCamera.data.Operation
import com.example.wowCamera.data.OperationType
import com.example.wowCamera.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_process.*
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.popup_window.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.sqrt

class ProcessActivity : BaseActivity() {

    private var predictStyleModel: PredictStyleModel? = null
    private var transferStyleModel: TransferStyleModel? = null

    val idList = arrayListOf(
        R.id.pattern_bjs,
        R.id.pattern_bjs2,
        R.id.pattern_cxp2,
        R.id.pattern_dfq,
        R.id.pattern_fg_xq,
        R.id.pattern_fsh,
        R.id.pattern_mnls,
        R.id.pattern_npl,
        R.id.pattern_xhc
    )
    var isSave = false
    val imagesList = arrayListOf(
        R.mipmap.bjs,
        R.mipmap.bjs2,
        R.mipmap.cxp2,
        R.mipmap.dfq,
        R.mipmap.fg_xk,
        R.mipmap.fsh,
        R.mipmap.mnls,
        R.mipmap.npl,
        R.mipmap.xhc
    )

    val stackUndo = Stack<Operation>() // AI换天为0，风格迁移为1，智能去躁为2，人像增强为3  滤镜为5
    var stackRedo = Stack<Operation>()
    val patternList = ArrayList<pattern>()//自定义模板的列表
    val patternImgUrlList = ArrayList<String>()
    val adapter = patternAdapter(patternList)
    var patternName = "1";
    var SQL_VERSION = 3//数据库版本


    private
    companion object {
        const val TAG = "xdy"
        val REQUIRED_PERMISSIONS =
            arrayOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onStart() {
        super.onStart()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.statusBarColor = resources.getColor(R.color.black);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", 3)
        dbHelper.writableDatabase
        contentValuesOf("" to "")
        initPatternSql(dbHelper)

        var strUri = intent.getSerializableExtra("uri").toString()
        var imgId = intent.getStringExtra("imgId").toString()
        var rotate = BitmapTools.getExifOrientation(strUri)
        var currentBitmap = BitmapFactory.decodeFile(strUri)

        currentBitmap = BitmapTools.rotate(currentBitmap, rotate)
        var imageShow: ImageView = findViewById<ImageView>(R.id.imageShow)
        var styleModel = AiUnitStyle(this.applicationContext)
        var skyModel = AiUnitSky(this.applicationContext)
        var pointShow: TextView = findViewById<TextView>(R.id.pointShow)
        var aesModel = AiUnitAes(this.applicationContext)
        aesModel.connect()
        //添加适配器
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL;//横向布局
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        imageShow.setImageBitmap(currentBitmap)
//        var originPoint = intent.getStringExtra("score")
        var originPoint = getPoint(aesModel,currentBitmap)
        var point = originPoint

        pointShow.setText(point.toString())
        stackUndo.push(Operation(OperationType.noProcess, currentBitmap, point.toString()))

        bindStyleListener(aesModel)

        back_ib.setOnClickListener {
            if (!isSave) {
                val msg = "您的图片未保存，是否退出？"
                val builder = AlertDialog.Builder(this, R.style.AlertDialog)
                    .setMessage(msg)
                    .setCancelable(true)
                    .setPositiveButton("确定退出") { _, _ ->
                        this.finish()
                    }
                    .setNeutralButton("取消", null)
                    .create()
                    .show()
            } else {
                this.finish()
            }
        }

        sky_card.setOnClickListener {

            messagevoid(1)
            Log.d(TAG, "visible")
            sky_card.isClickable = false
            sky_card.alpha = 0.3F
            skyModel.process(currentBitmap) { bitmap ->
                runOnUiThread {
                    imageShow.setImageBitmap(bitmap)
                    Toast.makeText(this, "魔法换天成功", Toast.LENGTH_SHORT).show()
                    dismissLoadingDialog()
                }
                currentBitmap = bitmap
                point = getPoint(aesModel, bitmap)
                pointShow.text = point

                stackUndo.push(Operation(OperationType.skyProcess, bitmap, point.toString()))
            }
        }

        style_card.setOnClickListener {
            messagevoid(1)
            style_card.isClickable = false
            style_card.alpha = 0.3F
            styleModel.process(currentBitmap) { bitmap ->
                runOnUiThread {
                    imageShow.setImageBitmap(bitmap)
                    Toast.makeText(this, "风格转换成功", Toast.LENGTH_SHORT).show()
                    dismissLoadingDialog()
                }
                currentBitmap = bitmap
                point = getPoint(aesModel, bitmap)
                pointShow.text = point
                stackUndo.push(Operation(OperationType.styleProcess, bitmap, point.toString()))

            }
        }


        noise_card.setOnClickListener {
            messagevoid(3)
            noise_card.isClickable = false
            noise_card.alpha = 0.3F

            var tempBitmap = stackUndo.peek().img
            var widthSize = tempBitmap.width
            var heightSize = tempBitmap.height

            Log.d(TAG, "origin size: width: $widthSize, height: $heightSize")

            if (widthSize * heightSize >= 2073600) {
                val rotation = sqrt((2073600.0 / (widthSize * heightSize)))
                val scaleWidth = (widthSize * rotation).toInt()
                val scaleHeight = (heightSize * rotation).toInt()
                Log.d(TAG, "resize后: width:$scaleWidth height: $heightSize")
                val smallBitmap = Bitmap.createScaledBitmap(
                    tempBitmap,
                    scaleWidth, scaleHeight, true
                )
                    .copy(Bitmap.Config.ARGB_8888, true)
                tempBitmap = smallBitmap
            }

            HttpUtils.sendImageForRequest(
                this,
                ":5000/uploadFile/denoise",
                tempBitmap,
                true,
                object : ImageListener {

                    override fun success(bitmap: Bitmap?, string: String?) {
                        point = getPoint(aesModel, bitmap!!)
                        Log.d("QXY", "success:Point: ${point}")
                        stackUndo.push(Operation(OperationType.denoiseProcess, bitmap, point.toString()))
                        runOnUiThread {
                            Toast.makeText(this@ProcessActivity, "智能去躁成功", Toast.LENGTH_SHORT)
                                .show()
                            imageShow.setImageBitmap(bitmap)
                            pointShow.text = point
                        }
                        dismissLoadingDialog()
                    }

                    override fun error(code: Int) {
                        runOnUiThread {
                            noise_card.isClickable = true
                            noise_card.alpha = 1F
                            Toast.makeText(this@ProcessActivity, "去噪失败，请检查网络连接", Toast.LENGTH_SHORT)
                                .show()
                            dismissLoadingDialog()
                        }
                    }
                })
        }

        enhance_card.setOnClickListener {
            messagevoid(1)
            enhance_card.isClickable = false
            enhance_card.alpha = 0.3F
            HttpUtils.sendImageForRequest(
                this,
                ":8080/file/process/enhance",
                stackUndo.peek().img,
                true,
                object : ImageListener {
                    override fun success(bitmap: Bitmap?, string: String?) {
                        point = getPoint(aesModel, bitmap!!)
                        stackUndo.push(Operation(OperationType.enhanceProcess, bitmap, point.toString()))
                        runOnUiThread {
                            Toast.makeText(this@ProcessActivity, "人像增强成功", Toast.LENGTH_SHORT)
                                .show()
                            imageShow.setImageBitmap(bitmap)
                            pointShow.text = point
                        }
                        dismissLoadingDialog()
                    }

                    override fun error(code: Int) {
                        runOnUiThread {
                            enhance_card.isClickable = true
                            enhance_card.alpha = 1F
                            Toast.makeText(this@ProcessActivity, "增强失败，请检查网络连接", Toast.LENGTH_SHORT)
                                .show()
                            dismissLoadingDialog()
                        }
                    }
                })
        }

        poem_iv.setOnClickListener {
            messagevoid(2)
            val smallBitmap = Bitmap.createScaledBitmap(stackUndo.peek().img, 128, 128, true)
                .copy(Bitmap.Config.ARGB_8888, true)
            HttpUtils.sendImageForRequest(
                this,
                ":8080/file/process/poem",
                smallBitmap,
                false,
                object : ImageListener {
                    override fun success(bitmap: Bitmap?, string: String?) {
                        dismissLoadingDialog()
                        runOnUiThread {
                            showPopWindow(string.toString(), 1, process_layout_1)
                        }
                    }

                    override fun error(code: Int) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ProcessActivity,
                                "AI作诗失败，请检查网络连接",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismissLoadingDialog()
                        }
                    }
                })
        }

        redo_iv.setOnClickListener {
            if (stackRedo.isNotEmpty()) {
                val stackTopOperation = stackRedo.pop()
                stackUndo.push(stackTopOperation)
                when (stackTopOperation.operation) {
                    OperationType.skyProcess-> {
                        sky_card.isClickable = false
                        sky_card.alpha = 0.3f
                    }
                    OperationType.styleProcess -> {
                        style_card.isClickable = false
                        style_card.alpha = 0.3f
                    }
                    OperationType.denoiseProcess -> {
                        noise_card.isClickable = false
                        noise_card.alpha = 0.3f
                    }
                    OperationType.enhanceProcess -> {
                        enhance_card.isClickable = false
                        enhance_card.alpha = 0.3f
                    }
                }
                currentBitmap = stackTopOperation.img
                pointShow.text = stackTopOperation.score
                imageShow.setImageBitmap(currentBitmap)
            } else {
                Toast.makeText(this, "已经无法重做", Toast.LENGTH_SHORT).show()
            }
        }

        undo_iv.setOnClickListener {
            if (stackUndo.size >= 2) {
                val stackOperation = stackUndo.pop()
                stackRedo.push(stackOperation)
                when (stackOperation.operation) {
                    OperationType.skyProcess -> {
                        sky_card.isClickable = true
                        sky_card.alpha = 1f
                    }
                    OperationType.styleProcess -> {
                        style_card.isClickable = true
                        style_card.alpha = 1f
                    }
                    OperationType.denoiseProcess -> {
                        noise_card.isClickable = true
                        noise_card.alpha = 1f
                    }
                    OperationType.enhanceProcess -> {
                        enhance_card.isClickable = true
                        enhance_card.alpha = 1f
                    }
                }
                currentBitmap = stackUndo.peek().img
                pointShow.text = stackUndo.peek().score
                imageShow.setImageBitmap(currentBitmap)
            } else {
                Toast.makeText(this, "已经为原图", Toast.LENGTH_SHORT).show()
            }

        }
        save.setOnClickListener {
            isSave = true
            val fileName =
                getOutputDirectory().absolutePath + "/" + SimpleDateFormat(FILENAME_FORMAT).format(
                    System.currentTimeMillis()
                ) + "_processed.jpg"
            val file = File(fileName)
            val bitmap = stackUndo.peek().img
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            updatePhoto(dbHelper,imgId,fileName,stackUndo.peek().score)
            Toast.makeText(this, "图片已保存", Toast.LENGTH_SHORT).show()

        }
        var patternState = false;//控制模板是否显示
        paiting_card.setOnClickListener {
            patternState = !patternState
            if (patternState) {
                paiting_layout2.visibility = View.VISIBLE;
            } else {
                paiting_layout2.visibility = View.GONE;
            }
            //加载模板类
            predictStyleModel = PredictStyleModel(this)
            transferStyleModel = TransferStyleModel(this)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            val all_selection = arrayOf(ai_layout, online_layout, config_layout, tools_layout)
            override fun onTabSelected(tab: TabLayout.Tab?) {
                var position = tab!!.position
                Log.d("xdy", "selected$position")
                all_selection[position].visibility = View.VISIBLE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                var position = tab!!.position
                Log.d("xdy", "unselected$position")
                all_selection[position].visibility = View.GONE

                patternState = false
                paiting_layout2.visibility = View.GONE

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                var position = tab!!.position
                Log.d("xdy", "reselected$position")
            }
        })

        config_iv.setOnClickListener {
            val userData = getSharedPreferences("data", Context.MODE_PRIVATE)
            val token = userData.getString("token", "invalide token")
            Log.d(TAG, "token: $token")
            if (token.equals("") || token.equals("invalide token")) {
                Toast.makeText(this, "请先登录再选择配置", Toast.LENGTH_SHORT).show()
            } else {
                val json = JSONObject()
                json.put("token", token)  // 调试时使用
                if (token != null) {
                    HttpUtils.sendConfig(json, 1) { json ->
                        run {
                            if (json != "null") {
                                val data = JSONArray(json)
                                var configStyles = ArrayList<ConfigStyle>()
                                val length = data.length()
                                for (index in 0 until length) {
                                    var config = data.getJSONObject(index)
                                    configStyles.add(
                                        ConfigStyle(
                                            config.get("name") as String,
                                            config.get("operation") as String,
                                            config.get("id") as Int
                                        )
                                    )
                                }
                                var dialogAdapter = DialogAdapter(this, configStyles)
                                val view =
                                    LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
                                if(length == 0) {
                                    view.title_tv.visibility = View.VISIBLE
                                }
                                val layoutManager = LinearLayoutManager(this)
                                layoutManager.orientation = LinearLayoutManager.VERTICAL;//横向布局
                                val recyclerView = view.findViewById<RecyclerView>(R.id.lv)
                                recyclerView.layoutManager = layoutManager
                                recyclerView.adapter = dialogAdapter
                                dialogAdapter.setOnItemClickListener(object : DialogAdapter.OnItemClickListener {
                                    override fun onItemClick(config: String) {
                                        val stackOperationType: Stack<Int> = Stack()
                                        val list = config.split(" ")
                                        stackOperationType.push(OperationType.noProcess.getType())
                                        for (i in list.size-1 downTo 1)
                                            if (list[i].isNotEmpty())
                                                stackOperationType.push(list[i].toInt())
                                        if(stackOperationType.size>1) {
                                            messagevoid(1)
                                        }else return
                                        processConfig(currentBitmap, skyModel, styleModel, stackOperationType) {
                                                bitmap -> run {
                                            if (bitmap!=null){
                                                Log.d("wyf", "返回成功")
                                                this@ProcessActivity.imageShow.setImageBitmap(bitmap)
                                                val point = getPoint(aesModel,bitmap)
                                                stackUndo.push(Operation(OperationType.configProcess,bitmap,point))
                                                runOnUiThread{ pointShow.text = point}
                                                Toast.makeText(this@ProcessActivity, "配置成功", Toast.LENGTH_SHORT).show()
                                                dismissLoadingDialog()
                                            }else{
                                                Log.d("wyf", "返回失败")
                                                Toast.makeText(
                                                    this@ProcessActivity,
                                                    "配置失败",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                dismissLoadingDialog()
                                            }
                                        }
                                        }
                                    }
                                })
                                val cancelConfig = view.findViewById<MaterialButton>(R.id.cancle_config)
                                val popupWindow: PopupWindow
                                popupWindow = PopupWindow(
                                    view, ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                popupWindow.setOnDismissListener {
                                    runOnUiThread {
                                        Toast.makeText(this, "您的配置已更新", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                popupWindow.isOutsideTouchable = true;
                                popupWindow.animationStyle = android.R.style.Animation_Dialog;
                                popupWindow.update();
                                popupWindow.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                popupWindow.isTouchable = true;
                                popupWindow.showAtLocation(process_layout_1, Gravity.CENTER, 0, 0)
                                cancelConfig.setOnClickListener {
                                    popupWindow.dismiss()
                                }
                            }
                        }
                    }
                }
            }
        }

        save_Config_iv.setOnClickListener {
            val userData = getSharedPreferences("data", Context.MODE_PRIVATE)
            val token = userData.getString("token", "invalide token")
            if (token.equals("")||token.equals("invalide token")){
                Toast.makeText(this,"请先登录再保存配置!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val popupWindow: PopupWindow
            val pview = LayoutInflater.from(this).inflate(R.layout.popup_window, null)
            popupWindow = PopupWindow(
                pview,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.contentView = pview
            popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            val inputMethodManager =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS) //这里给它设置了弹出的时间
            val editText = pview.findViewById<EditText>(R.id.editText)
            editText.requestFocus()
            editText.setText("请在此输入保存配置的名字")
            editText.selectAll()
            val confirm = pview.findViewById<Button>(R.id.confirm)
            val cancel = pview.findViewById<Button>(R.id.cancle)

            //显示PopupWindow
            val rootview = LayoutInflater.from(this).inflate(R.layout.activity_process, null)
            popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)
            cancel.setOnClickListener {
                popupWindow.dismiss() //让PopupWindow消失
            }
            confirm.setOnClickListener {
                var name = "none"
                val json = JSONObject()
                val inputString = editText.text.toString()
                name = inputString
                var operation = ""
                json.put("token", token)
                json.put("name", name)
                val elements: Enumeration<Operation> = stackUndo.elements()
                while (elements.hasMoreElements()) {
                    val code = elements.nextElement().operation.getType().toString()
                    operation += code + " "
                }
                json.put("operation", operation)
                Log.d(TAG, "operation: $operation")
                if (token != null) {
                    HttpUtils.sendConfig(json, 2) { info ->
                        if (info == "null")
                            Toast.makeText(this, "请检查您的网络连接", Toast.LENGTH_SHORT).show()
                        else {
                            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                popupWindow.dismiss() //让PopupWindow消失
            }
        }

        //添加模板按钮
        pattern_add.setOnClickListener {
            //弹窗——网络、本地、取消——popwindow
            //本地——选择图片，保存，加入adapter
            val popupWindow: PopupWindow
            val pview = LayoutInflater.from(this).inflate(R.layout.pattern_add_popup_window, null)
            popupWindow = PopupWindow(
                pview,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.contentView = pview
            popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE


            val from_internet = pview.findViewById<Button>(R.id.from_internet)
            val from_local = pview.findViewById<Button>(R.id.from_local)
            val edit_add = pview.findViewById<Button>(R.id.edit_add)

            //显示PopupWindow
            val rootview =
                LayoutInflater.from(this).inflate(R.layout.activity_process, null)
            popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)

            from_internet.setOnClickListener {

                val popupWindow: PopupWindow
                val pview = LayoutInflater.from(this@ProcessActivity)
                    .inflate(R.layout.popup_window, null)
                popupWindow = PopupWindow(
                    pview,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.contentView = pview
                popupWindow.softInputMode =
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                val editText = pview.findViewById<EditText>(R.id.editText)
                val confirm = pview.findViewById<Button>(R.id.confirm)
                val cancle = pview.findViewById<Button>(R.id.cancle)
                pview.rename_title.text = "网络模型"
                pview.rename_title_small.text = "输入分享字符串"
                //显示PopupWindow
                val rootview =
                    LayoutInflater.from(this@ProcessActivity)
                        .inflate(R.layout.activity_process, null)
                popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)
                confirm.setOnClickListener {
                    val InternetPattern = editText.getText().toString()
                    val url = ":8080/template/getTemplate/${InternetPattern}"
                    //发送字符串至网络
                    messagevoid(1)
                    HttpUtils.sendStringForRequest(
                        url,
                        object : ImageListener {
                            override fun success(bitmap: Bitmap?, string: String?) {
                                Toast.makeText(this@ProcessActivity, "添加网络模板成功", Toast.LENGTH_SHORT)
                                    .show()
                                runOnUiThread {
                                    if (bitmap != null) {
                                        Log.d(
                                            TAG,
                                            "接收图片width: ${bitmap.width} height: ${bitmap.height}"
                                        )
                                        addAdapter(bitmap, InternetPattern)
                                        popupWindow.dismiss()
                                    }
                                    dismissLoadingDialog()
                                }

                            }

                            override fun error(code: Int) {
                                runOnUiThread {
                                    noise_card.isClickable = true
                                    noise_card.alpha = 1F
                                    Toast.makeText(
                                        this@ProcessActivity,
                                        "添加失败，请检查网络连接",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    dismissLoadingDialog()
                                }
                            }
                        })
                    popupWindow.dismiss()
                }
                cancle.setOnClickListener {
                    popupWindow.dismiss()
                }
            }

            from_local.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, 1)
                popupWindow.dismiss()
            }
            edit_add.setOnClickListener {
                popupWindow.dismiss()
            }
        }

        adapter.setOnItemClickListener(
            object : patternAdapter.OnItemClickListener {
                override fun onItemLongClick(view: View?, position: Int) {
                    val popupWindow: PopupWindow
                    val pview = LayoutInflater.from(this@ProcessActivity)
                        .inflate(R.layout.pattern_edit_popup_window, null)
                    popupWindow = PopupWindow(
                        pview,
                        ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        true
                    )
                    popupWindow.contentView = pview
                    popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE


                    val pattern_rename = pview.findViewById<Button>(R.id.pattern_rename)
                    val pattern_delete = pview.findViewById<Button>(R.id.pattern_delete)
                    val pattern_share = pview.findViewById<Button>(R.id.pattern_share)

                    pattern_rename.setOnClickListener {
                        showRenamePopWindow(position)
                    }
                    pattern_delete.setOnClickListener {
                        deletePattern(dbHelper,position)
                        patternList.removeAt(position)
                        patternImgUrlList.removeAt(position)
                        adapter.notifyDataSetChanged()
                        popupWindow.dismiss()
                    }

                    pattern_share.setOnClickListener {
                        var paName = adapter.patternList[position].name
                        val userData = getSharedPreferences("data", Context.MODE_PRIVATE)
                        val token = userData.getString("token", "")
                        Log.d(TAG, "token: $token")
                        if (token == "") {
                            Toast.makeText(this@ProcessActivity, "请先登录再分享", Toast.LENGTH_SHORT).show()
                        } else {
                            messagevoid(1)
                            val bitmap = adapter.patternList[position].imgBitmap
                            val width = bitmap.width
                            val height = bitmap.height
                            val smallBitmap =
                                Bitmap.createScaledBitmap(bitmap, 300, height * 300 / width, true)
                                    .copy(Bitmap.Config.ARGB_8888, true)
                            HttpUtils.sendImageForRequest(
                                this@ProcessActivity,
                                ":8080/template/saveTemplate/${token}/${paName}",
                                smallBitmap,
                                false,
                                object : ImageListener {
                                    override fun success(bitmap: Bitmap?, string: String?) {
                                        Toast.makeText(
                                            this@ProcessActivity,
                                            "分享成功$string",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        dismissLoadingDialog()
                                        runOnUiThread {
                                            showPopWindow(string.toString(), 2, process_layout_1)
                                        }
                                    }
                                    override fun error(code: Int) {
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@ProcessActivity,
                                                "分享失败，请检查网络连接",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            dismissLoadingDialog()
                                        }
                                    }
                                })
                        }
                    }
                    //显示PopupWindow
                    val rootview =
                        LayoutInflater.from(this@ProcessActivity)
                            .inflate(R.layout.activity_process, null)
                    popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)

                }

                override fun onItemClick(view: View?, position: Int) {
                    Log.d(TAG, "onItemClick: " + patternList[position].imgBitmap.toString())
                    //点击自定义模板
                    val styleBitmap = patternList[position].imgBitmap
                    if (predictStyleModel != null && transferStyleModel != null) {
                        if (stackUndo.peek().operation.getType() >= 500) {//使用过了滤镜
                            stackUndo.pop()
                        }
                        messagevoid(1)
                        thread {
                            val originBitmap = stackUndo.peek().img
                            val styleBuffer = predictStyleModel!!.getStyleBottleneck(styleBitmap)
                            val originStyleBuffer = predictStyleModel!!.getStyleBottleneck(originBitmap)
                            val styleBitmap = transferStyleModel!!.getTransformImage(
                                originStyleBuffer,
                                styleBuffer,
                                originBitmap
                            )
                            var point = getPoint(aesModel, styleBitmap)
                            var OpNum = 510 + position
                            OperationType.AItransfer.changeAICode(OpNum)
                            Log.d(TAG, "AdapteronItemClick: " + OpNum.toString())
                            stackUndo.push(Operation(OperationType.AItransfer, styleBitmap, point))
                            runOnUiThread {
                                dismissLoadingDialog()
                                imageShow.setImageBitmap(styleBitmap)
                                pointShow.text = point
                            }
                        }
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // 将选择的照片显示
                        val simpleDateFormat = SimpleDateFormat("MMdd_HHmm")
                        val simpleDateFormat2 = SimpleDateFormat("MMdd_HHmmssSS")
                        val date = Date(System.currentTimeMillis())
                        patternName = simpleDateFormat.format(date).toString()

                        val bitmap = getBitmapFromUri(uri)
//                        val fileName =
//                            getOutputDirectory().absolutePath + "/" + patternName + ".jpg"
                        val fileName =
                            getOutputDirectory().absolutePath + "/" + simpleDateFormat2.format(date).toString() + ".jpg"
                        patternImgUrlList.add(fileName)//文件路径添加
                        Log.d(TAG, "patternImgUrlList: $patternImgUrlList")
                        val file = File(fileName)
                        val fos = FileOutputStream(file)
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        patternList.add(pattern(patternName.toString(), bitmap))
                        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", 3)
                        addPattern(dbHelper,patternName.toString(),fileName)
                        adapter.notifyDataSetChanged()
                        fos.flush()
                        fos.close()
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        predictStyleModel?.close()
        transferStyleModel?.close()
    }

    private fun bindStyleListener(aesModel: AiUnitAes) {
        for (index in 0 until idList.size) {
            findViewById<CardView>(idList[index]).setOnClickListener {
                val imgId = imagesList[index]
                val styleBitmap = BitmapFactory.decodeResource(resources, imgId, null)
                if (predictStyleModel != null && transferStyleModel != null) {
                    if (stackUndo.peek().operation.getType() >= 500) {//使用过了滤镜
                        stackUndo.pop()
                    }
                    messagevoid(1)
                    thread {
                        val originBitmap = stackUndo.peek().img
                        val styleBuffer = predictStyleModel!!.getStyleBottleneck(styleBitmap)
                        val originStyleBuffer =
                            predictStyleModel!!.getStyleBottleneck(originBitmap)

                        val styleBitmap = transferStyleModel!!.getTransformImage(
                            originStyleBuffer,
                            styleBuffer,
                            originBitmap
                        )
                        var point = getPoint(aesModel, styleBitmap)
                        var operationNum = 500 + index
                        OperationType.AItransfer.changeAICode(operationNum)
                        Log.d(TAG, "bindStyleListener: $operationNum")
                        stackUndo.push(Operation(OperationType.AItransfer, styleBitmap, point))
                        runOnUiThread {
                            dismissLoadingDialog()
                            imageShow.setImageBitmap(styleBitmap)
                            pointShow.text = point
                        }
                    }
                }
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    fun processConfig(bitmap: Bitmap,skyModel: AiUnitSky,styleModel: AiUnitStyle,config:Stack<Int>,listener:(bitmap:Bitmap?)->Unit){
        try {
            val configFlag = config.pop()
            var bitmapTemp = bitmap
            if(configFlag>=500){
                predictStyleModel = PredictStyleModel(this)
                transferStyleModel = TransferStyleModel(this)
                val position = configFlag-500
                val styleBitmap = BitmapFactory.decodeResource(resources,imagesList[position])
                val styleBuffer = predictStyleModel!!.getStyleBottleneck(styleBitmap)
                val originStyleBuffer = predictStyleModel!!.getStyleBottleneck(bitmapTemp)
                val bitmapTemp = transferStyleModel!!.getTransformImage(originStyleBuffer, styleBuffer, bitmapTemp)
                processConfig(bitmapTemp,skyModel,styleModel,config){ listener(it) }
            }else
                when(configFlag){
                    OperationType.skyProcess.getType()->{ skyModel.process(bitmap){ bitmapReturn->
                        processConfig(bitmapReturn,skyModel,styleModel,config){
                            listener(it) } } }
                    OperationType.styleProcess.getType()->{ styleModel.process(bitmap){ bitmapReturn->
                        processConfig(bitmapReturn,skyModel,styleModel,config){
                            listener(it) } } }
                    OperationType.denoiseProcess.getType()->{ HttpUtils.sendImageForRequest(this@ProcessActivity,":5000/uploadFile/denoise", bitmapTemp, true,object:ImageListener{
                        override fun success(bitmap: Bitmap?, string: String?) {
                            if (bitmap != null) {
                                processConfig(bitmap,skyModel,styleModel,config){ listener(it) }
                            }else listener(null)
                        }
                        override fun error(code: Int) {
                            listener(null)
                        }
                    }) }
                    OperationType.enhanceProcess.getType()->{ HttpUtils.sendImageForRequest(this@ProcessActivity, ":8080/file/process/enhance", bitmapTemp, true,object:ImageListener{
                        override fun success(bitmap: Bitmap?, string: String?) {
                            if (bitmap != null) {
                                processConfig(bitmap,skyModel,styleModel,config){ listener(it) }
                            }else listener(null)
                        }
                        override fun error(code: Int) {
                            listener(null)
                        }
                    }) }
                    OperationType.noProcess.getType()->{
                        listener(bitmapTemp)
                        return
                    }
                }
        }catch (e:Exception){
            Log.d("WYF","ConfigProcessFail")
            throw e
        }
    }







    fun addAdapter(bitmap: Bitmap, name: String) {
        val fileName =
            getOutputDirectory().absolutePath + "/" + name + ".jpg"
        patternImgUrlList.add(fileName)//文件路径添加
        val file = File(fileName)
        val fos = FileOutputStream(file)
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        var name_show = name.substring(0,9)

        patternList.add(pattern(name_show, bitmap))
        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", 3)
        addPattern(dbHelper,name_show,fileName)
        adapter.notifyDataSetChanged()

        fos.flush()
        fos.close()
    }

    fun showRenamePopWindow(position: Int) {
        val popupWindow: PopupWindow
        val pview = LayoutInflater.from(this).inflate(R.layout.popup_window, null)
        popupWindow = PopupWindow(
            pview,
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.contentView = pview
        popupWindow.softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        val editText = pview.findViewById<EditText>(R.id.editText)
        val confirm = pview.findViewById<Button>(R.id.confirm)
        val cancle = pview.findViewById<Button>(R.id.cancle)
        //显示PopupWindow
        val rootview =
            LayoutInflater.from(this)
                .inflate(R.layout.activity_process, null)
        popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0)
        confirm.setOnClickListener {
            val newName = editText.getText().toString()
            patternList.get(position).setName(newName)
            val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
            updatePattern(dbHelper,position,newName)
            adapter.notifyDataSetChanged()
            popupWindow.dismiss()
        }
        cancle.setOnClickListener {
            popupWindow.dismiss()
        }
    }
    fun initPatternSql(dbHelper:MyDatabaseHelper){
        val db = dbHelper.writableDatabase

        val cursor = db.query("Pattern", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                val pName = cursor.getString(cursor.getColumnIndex("name"))
                val pid = cursor.getString(cursor.getColumnIndex("id"))
                val pImgUrl = cursor.getString(cursor.getColumnIndex("imgUrl"))
                val bitmap = BitmapFactory.decodeFile(pImgUrl)
                if (pImgUrl != null) {
                    patternImgUrlList.add(pImgUrl)
                    patternList.add(pattern(pName, bitmap))
                }//文件路径添加

            } while (cursor.moveToNext())
        }
        cursor.close()
    }
    fun addPattern(dbHelper:MyDatabaseHelper,pName:String,pImgUrl:String){
        val db = dbHelper.writableDatabase
        contentValuesOf("" to "")
        val values = ContentValues().apply {
            put("name", pName)
            put("imgUrl", pImgUrl)
        }
        db.insert("Pattern", null, values) // 插入数据
    }

    fun deletePattern(dbHelper:MyDatabaseHelper,position: Int){
        val db = dbHelper.writableDatabase

        var cursor = db.query("Pattern",null,null,null,null,null,null,"${position},1")
        cursor.moveToFirst();
        var pid = cursor.getString(cursor.getColumnIndex("id"))
        db.execSQL("delete from Pattern where id = ?", arrayOf(pid))
    }
    fun updatePattern(dbHelper:MyDatabaseHelper,position: Int,newName:String){
        val db = dbHelper.writableDatabase
        var cursor = db.query("Pattern",null,null,null,null,null,null,"${position},1")
        cursor.moveToFirst();
        var pid = cursor.getString(cursor.getColumnIndex("id"))

        db.execSQL("update Pattern set name = ? where id = ?", arrayOf(newName,pid))

    }
    fun updatePhoto(dbHelper:MyDatabaseHelper,id:String,imgUrl:String,score:String){
        val db = dbHelper.writableDatabase
        if (id =="carema"){
            var  fotime = "albumid"
            contentValuesOf("" to "")
            val values = ContentValues().apply {
                put("imgurl", imgUrl)
                put("score", score)
                put("fotime", fotime)
            }
            db.insert("Album", null, values) // 插入数据
        }else{
            db.execSQL("update Album set imgurl = ?,score = ? where id = ?", arrayOf(imgUrl,score,id))
        }

    }
}