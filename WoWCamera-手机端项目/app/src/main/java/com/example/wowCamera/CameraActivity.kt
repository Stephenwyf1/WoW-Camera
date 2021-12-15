package com.example.wowCamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.*
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wowCamera.utils.*
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_camera.view.*
import java.io.File
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


class CameraActivity : BaseActivity() {

    companion object {
        private const val TAG = "CameraXTest" // 调试TAG
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS" // 照片名称格式
        private const val REQUEST_CODE_PERMISSIONS = 10 // 请求授权的intent的requestCode
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE) // 所需所有授权
    }

    private var imageCapture: ImageCapture? = null // 拍照
    private lateinit var outputDirectory: File // 输出路径
    private lateinit var cameraExecutor: ExecutorService // 相机线程执行者
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // 相机选择，默认后置
    private var cameraRatio = AspectRatio.RATIO_4_3 // 相机比例，默认4:3
    private var flashMode = FLASH_MODE_OFF // 闪光灯默认关闭
    private var showNet = View.INVISIBLE // 网格默认不可见
    private var p_value = 30 // 灵敏度
    lateinit var model: AiUnitAes
    lateinit var poseNet: PoseNet
    private lateinit var poseBitmap: Bitmap
    private lateinit var standardPoseData: ArrayList<HashMap<Int, PoseKeyPoint>> // 所有标准动作库
    private var standardPose: HashMap<Int, PoseKeyPoint>? = null //  当前动作库
    private var poseLine = false

    override fun onStart() {
        super.onStart()
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        model = AiUnitAes(this.applicationContext)
        model.connect()
    }

    override fun onResume() {
        super.onResume()
        this.window.decorView.apply {
            systemUiVisibility =
                SYSTEM_UI_FLAG_IMMERSIVE or SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        standardPoseData = JsonUtils.getJsonData("data.json", this)

        poseNet = PoseNet(this)
        poseBitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
        poseBitmap.eraseColor(Color.argb(0, 0, 0, 0))
        pose_iv.setImageBitmap(poseBitmap)

        val poseMaskCardList = arrayListOf(
            pose_1_card,
            pose_2_card,
            pose_3_card,
            pose_4_card,
            pose_5_card,
            pose_6_card,
            pose_7_card
        )
        val poseMaskImageList = arrayListOf(
            R.mipmap.pose_mask_1,
            R.mipmap.pose_mask_2,
            R.mipmap.pose_mask_3,
            R.mipmap.pose_mask_4,
            R.mipmap.pose_mask_5,
            R.mipmap.pose_mask_6,
            R.mipmap.pose_mask_7
        )

        // 检查权限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        // 拍照按钮
        camera_capture_button.setOnClickListener {
            takePhoto()
        }

        // 初始引导
        val isFirstRun = sharedPreferences.getBoolean("isFirstRunCamera", true)
        if(isFirstRun) {
//            Toast.makeText(this, "第一次运行", Toast.LENGTH_SHORT).show()
            lateinit var guideView1: GuideView
            lateinit var guideView2: GuideView
            lateinit var guideView3: GuideView
            lateinit var guideView4: GuideView
            val tv = TextView(this)
            tv.text = "按下此按钮将显示网格线"
            tv.setTextColor(resources.getColor(R.color.white))
            tv.textSize = 13f
            tv.gravity = Gravity.CENTER

            guideView1 = GuideView.Builder
                .newInstance(this)
                .setTargetView(net_iv)
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.LEFT_BOTTOM)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置椭圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView1.setOnclickListener {
                guideView1.hide()
                tv.text="按下此按钮将改变拍照比例"
                guideView2.show()
            }

            guideView2 = GuideView.Builder
                .newInstance(this)
                .setTargetView(camera_ratio) //设置目标
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.BOTTOM)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView2.setOnclickListener {
                guideView2.hide()
                tv.text = "按下此按钮将调整AI美学灵敏度为默认值"
                guideView3.show()
            }

            guideView3 = GuideView.Builder
                .newInstance(this)
                .setTargetView(default_btn) //设置目标tv
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.LEFT_TOP)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView3.setOnclickListener {
                guideView3.hide()
                tv.text = "按下此按钮切换前后置摄像头哦"
                guideView4.show()
            }

            guideView4 = GuideView.Builder
                .newInstance(this)
                .setTargetView(switch_camera) //设置目标
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.LEFT_TOP)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView4.setOnclickListener {
//                Toast.makeText(this@MainActivity, "引导结束", Toast.LENGTH_SHORT).show()
                guideView4.hide()
            }
            guideView1.show()

        }

        // 切换前后摄像头
        switch_camera.setOnClickListener {
            // 播放旋转动画
            val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
            rotateAnim.interpolator = LinearInterpolator()
            this.switch_camera.startAnimation(rotateAnim)
            cameraSelector = when (cameraSelector) {
                CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraSelector.DEFAULT_FRONT_CAMERA -> CameraSelector.DEFAULT_BACK_CAMERA
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            }
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA && sharedPreferences.getBoolean("isFirstRunCamera",true)){

                sharedPreferences.edit().putBoolean("isFirstRunCamera", false).apply()
                val tv = TextView(this)
                tv.text = "按下此按钮将开启智能姿态辅助摄影"
                tv.setTextColor(resources.getColor(R.color.white))
                tv.textSize = 13f
                tv.gravity = Gravity.CENTER
                val guideView = GuideView.Builder
                    .newInstance(this)
                    .setTargetView(pose_cb)
                    .setCustomGuideView(tv)
                    .setDirction(GuideView.Direction.RIGHT_TOP)
                    .setShape(GuideView.MyShape.CIRCULAR) // 设置椭圆形显示区域，
                    .setBgColor(resources.getColor(R.color.transparent))
                    .build()
                guideView.show()

                guideView.setOnclickListener {
                    guideView.hide()
                }
            }
            startCamera()
            togglePoseBtn()
        }
        // 切换拍照比例
        camera_ratio.setOnClickListener {
            val newLayoutParams = viewFinder.layoutParams as (ConstraintLayout.LayoutParams)
            val newLayoutParams2 =
                net_constraintLayout.layoutParams as (ConstraintLayout.LayoutParams)
            when (cameraRatio) {
                AspectRatio.RATIO_4_3 -> {
                    cameraRatio = AspectRatio.RATIO_16_9
                    newLayoutParams.dimensionRatio = "9:16"
                    newLayoutParams2.dimensionRatio = "9:16"
                    camera_ratio.text = "16:9"
                }
                AspectRatio.RATIO_16_9 -> {
                    cameraRatio = AspectRatio.RATIO_4_3
                    newLayoutParams.dimensionRatio = "3:4"
                    newLayoutParams2.dimensionRatio = "3:4"
                    camera_ratio.text = "4:3"
                }
            }
            viewFinder.layoutParams = newLayoutParams
            net_constraintLayout.layoutParams = newLayoutParams2
            startCamera()
        }

        flash_iv.setOnClickListener {
            when (flashMode) {
                FLASH_MODE_OFF -> {
                    Toast.makeText(this, "闪光灯: 开", Toast.LENGTH_SHORT).show()
                    flashMode = FLASH_MODE_ON
                    flash_iv.setImageResource(R.drawable.flash_on)
                }
                FLASH_MODE_ON -> {
                    Toast.makeText(this, "闪光灯: 自动", Toast.LENGTH_SHORT).show()
                    flashMode = FLASH_MODE_AUTO
                    flash_iv.setImageResource(R.drawable.flash_auto)
                }
                FLASH_MODE_AUTO -> {
                    Toast.makeText(this, "闪光灯: 关闭", Toast.LENGTH_SHORT).show()
                    flashMode = FLASH_MODE_OFF
                    flash_iv.setImageResource(R.drawable.flash_off)
                }
            }
            startCamera()
        }

        // 设置显示网格
        net_iv.setOnClickListener {
            when (showNet) {
                View.INVISIBLE -> {
                    showNet = View.VISIBLE
                    net_iv.setImageResource(R.drawable.net)
                    allNet.visibility = showNet
                }
                View.VISIBLE -> {
                    showNet = View.INVISIBLE
                    net_iv.setImageResource(R.drawable.net_close)
                    allNet.visibility = showNet
                }
            }
        }

        // 设置是否显示人体辅助线
        pose_line_iv.setOnClickListener {
            when (poseLine) {
                true -> {
                    pose_line_iv.setImageResource(R.drawable.ic_pose_line_close)
                }
                else -> {
                    pose_line_iv.setImageResource(R.drawable.ic_pose_line)
                }
            }
            poseLine = !poseLine
        }

        // 设置强度条
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                p_value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(applicationContext, "已调整灵敏度", Toast.LENGTH_SHORT).show()
            }
        })

        // 恢复灵敏度为默认值
        default_btn.setOnClickListener {
            seekBar.progress = 30
            Toast.makeText(applicationContext, "恢复默认", Toast.LENGTH_SHORT).show()
        }

        // 开关pose
        pose_cb.setOnCheckedChangeListener { _, flag ->
            run {
                pose_1_card.callOnClick()
                if (flag) {
                    pose_line_iv.visibility = View.VISIBLE
                    pose_mask_iv.visibility = View.VISIBLE
                    pose_bar.visibility = View.VISIBLE
                    pose_iv.visibility = View.VISIBLE
                    pose_sv.visibility = View.VISIBLE
                    not_connect_layout.visibility = View.INVISIBLE
                    location_layout.visibility = View.INVISIBLE
                    pose_point_layout.visibility = View.VISIBLE
                } else {
                    pose_line_iv.visibility = View.GONE
                    pose_mask_iv.visibility = View.INVISIBLE
                    pose_bar.visibility = View.INVISIBLE
                    pose_iv.visibility = View.INVISIBLE
                    pose_sv.visibility = View.INVISIBLE
                    poseBitmap.eraseColor(Color.argb(0, 0, 0, 0))
                    pose_iv.setImageBitmap(poseBitmap)
                    pose_point_layout.visibility = View.INVISIBLE
                    not_connect_layout.visibility = View.VISIBLE
                    location_layout.visibility = View.VISIBLE
                }
                toggleAllText()
            }
        }

        // 点击显示对应线框图
        for (idx in 0 until poseMaskCardList.size) {
            poseMaskCardList[idx].setOnClickListener {
                standardPose = standardPoseData[idx]
                pose_mask_iv.setImageResource(poseMaskImageList[idx])
            }
        }
    }

    private fun togglePoseBtn() {
        if (pose_cb.visibility == View.VISIBLE) {
            pose_cb.visibility = View.INVISIBLE
            pose_cb.isChecked = false
        } else {
            pose_cb.visibility = View.VISIBLE
        }
    }


    private fun toggleAllText() {
        var visibilityFlag = View.VISIBLE
        if (text_11.visibility == View.VISIBLE) {
            visibilityFlag = View.INVISIBLE
        }
        text_11.visibility = visibilityFlag
        text_12.visibility = visibilityFlag
        text_13.visibility = visibilityFlag
        text_21.visibility = visibilityFlag
        text_22.visibility = visibilityFlag
        text_23.visibility = visibilityFlag
        text_31.visibility = visibilityFlag
        text_32.visibility = visibilityFlag
        text_33.visibility = visibilityFlag
    }

    private fun initPreview() = Preview.Builder()
        .setTargetAspectRatio(cameraRatio)
        .build()
        .also {
            it.setSurfaceProvider(viewFinder.surfaceProvider)
        }

    private var preMaxIdx = 4
    private fun initAnalyzer() = ImageAnalysis.Builder()
        .setImageQueueDepth(1)
        .setTargetAspectRatio(cameraRatio)
        .build()
        .also {
            it.setAnalyzer(cameraExecutor, DataAnalyzer(object : AnalyseListener {
                override fun onScoreFinish(data: ArrayList<Float>) {
                    var maxScore = data[4]
                    var maxIdx = 4
                    var equalTimes = 1
                    for (i in 0..8) {
                        if ((data[i] - maxScore) > 0.4 - p_value / 1000.0) {
                            maxScore = data[i]
                            maxIdx = i
                        }
                    }
                    var showRotaionList = arrayOf(45F, 0F, -45F, 90F, 0F, -90F, 135F, 180F, -135F)
                    runOnUiThread {
                        not_connect_layout.visibility = View.INVISIBLE
                        location_layout.visibility = View.VISIBLE
                        unstable_group.visibility = View.INVISIBLE
                        point_group.visibility = View.INVISIBLE
                        great_group.visibility = View.INVISIBLE
                        if (maxIdx == preMaxIdx) {
                            equalTimes += 1
                        } else {
                            equalTimes == 1
                        }
                        if (equalTimes >= 2) {
                            if(maxIdx == 4) { // 最佳方向为中心
                                great_group.visibility = View.VISIBLE
                            } else {
                                point_group.visibility = View.VISIBLE
                                location_arrow_iv.rotation = showRotaionList[maxIdx]
                            }
                        } else {
                            unstable_group.visibility = View.VISIBLE
                        }

                        text_11.text = String.format("%.2f", changePoint(data[0].toDouble()))
                        text_12.text = String.format("%.2f", changePoint(data[1].toDouble()))
                        text_13.text = String.format("%.2f", changePoint(data[2].toDouble()))
                        text_21.text = String.format("%.2f", changePoint(data[3].toDouble()))
                        text_22.text = String.format("%.2f", changePoint(data[4].toDouble()))
                        text_23.text = String.format("%.2f", changePoint(data[5].toDouble()))
                        text_31.text = String.format("%.2f", changePoint(data[6].toDouble()))
                        text_32.text = String.format("%.2f", changePoint(data[7].toDouble()))
                        text_33.text = String.format("%.2f", changePoint(data[8].toDouble()))
                    }
                    preMaxIdx = maxIdx
                }

                override fun onScoreFail() {
                    runOnUiThread {
                        location_layout.visibility = View.INVISIBLE
                        not_connect_layout.visibility = View.VISIBLE
                    }
                }

                override fun onKeyPointFinish(data: ArrayList<KeyPoint>) {
                    if (poseLine) {
                        getWholeImage(data, poseBitmap.width, poseBitmap.height)
                    }
                    if (standardPose != null) {
                        var maxName = ""
                        var maxDistance = -1F
                        var deltaX = 0F
                        var deltaY = 0F
                        for (keyPoint in data) {
                            val index = keyPoint.index
                            if (standardPose!!.containsKey(index) && keyPoint.conf > PoseNet.threshold) {
                                val x1 = keyPoint.x
                                val y1 = keyPoint.y
                                val x2 = standardPose!![index]!!.x
                                val y2 = standardPose!![index]!!.y
                                val distance = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
                                if (distance > maxDistance) {
                                    maxDistance = distance
                                    maxName = PoseNet.keyPointNameForFront[index]
                                    deltaX = x2 - x1
                                    deltaY = y2 - y1
                                }
                            }
                        }
                        var showRotaionList = arrayOf(arrayOf(45F, -45F, 0F), arrayOf(135F, -135F, 180F), arrayOf(90F, -90F, 0F))
                        var stateX = 2
                        var stateY = 2
                        if (maxDistance > 30) {
                            if (deltaX.absoluteValue * 1.73 > deltaY.absoluteValue) {
                                if (deltaX > 0) {
                                    stateX = 1
                                } else {
                                    stateX = 0
                                }
                            }
                            if (deltaY.absoluteValue * 1.73 > deltaX.absoluteValue) {
                                if (deltaY > 0) {
                                    stateY = 1
                                } else {
                                    stateY = 0
                                }
                            }
                            runOnUiThread {
                                pose_point_group.visibility = View.VISIBLE
                                pose_great_group.visibility = View.INVISIBLE
                                warn_group.visibility = View.INVISIBLE
                                prompt_group.visibility = View.INVISIBLE
                                pose_point_part1_tv.text = "${maxName}向"
                                pose_arrow_iv.rotation = showRotaionList[stateX][stateY]
                            }
                        } else if (maxDistance == -1F) {
                            runOnUiThread {
                                warn_group.visibility = View.VISIBLE
                                prompt_group.visibility = View.INVISIBLE
                                pose_great_group.visibility = View.INVISIBLE
                                pose_point_group.visibility = View.INVISIBLE
                            }
                        } else {
                            runOnUiThread {
                                pose_great_group.visibility = View.VISIBLE
                                prompt_group.visibility = View.INVISIBLE
                                warn_group.visibility = View.INVISIBLE
                                pose_point_group.visibility = View.INVISIBLE
                            }
                        }
                    }
                    runOnUiThread {
                        pose_iv.setImageBitmap(poseBitmap)
                    }
                }
            }))
        }

    private fun initCapture() = Builder()
        .setFlashMode(flashMode)
        .setTargetAspectRatio(cameraRatio)
        .build()

    // 开始预览与分析
    private fun startCamera() {
        val executor = ContextCompat.getMainExecutor(this)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider =
                cameraProviderFuture.get() // 将camera的lifecycle与主进程绑定
            val preview = initPreview() // 预览
            val imageAnalyzer = initAnalyzer() // 分析
            imageCapture = initCapture() // 拍照
            // 设置图片旋转
            val orientationEventListener = object : OrientationEventListener(this) {
                override fun onOrientationChanged(orientation: Int) {
                    val rotation: Int = when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }
                    imageCapture!!.targetRotation = rotation
                    imageAnalyzer.targetRotation = rotation
                    runOnUiThread {
                        val newRotation = (rotation * 90).toFloat()
                        location_layout.rotation = newRotation
                        not_connect_layout.rotation = newRotation
                        pose_point_layout.rotation = newRotation
                        if(newRotation == 90F || newRotation == 270F) {
                            not_connect_layout.translationY = 120F
                            location_layout.translationY = 120F
                            pose_point_layout.translationY = 120F
                            pose_great_group.visibility = View.INVISIBLE
                            prompt_group.visibility = View.VISIBLE
                            warn_group.visibility = View.INVISIBLE
                            pose_point_group.visibility = View.INVISIBLE
                        } else {
                            not_connect_layout.translationY = 0F
                            location_layout.translationY = 0F
                            pose_point_layout.translationY = 0F
                        }
                        switch_camera.rotation = newRotation
                        camera_ratio.rotation = newRotation
                        flash_iv.rotation = newRotation
                        text_11.rotation = newRotation
                        text_12.rotation = newRotation
                        text_13.rotation = newRotation
                        text_21.rotation = newRotation
                        text_22.rotation = newRotation
                        text_23.rotation = newRotation
                        text_31.rotation = newRotation
                        text_32.rotation = newRotation
                        text_33.rotation = newRotation
                    }
                }
            }
            orientationEventListener.enable()
            try {
                cameraProvider.unbindAll() // 解绑之前所有的
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                ) //绑定
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, executor)
    }

    // 判断授权情况
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("isFirstRunCamera",true))
            sharedPreferences.edit().putBoolean("isFirstRunCamera", false).apply()
        cameraExecutor.shutdown()
        model.close()
//        poseNet.closeModel()
    }

    // 拍照
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        //设置输出路径
        val fileName = SimpleDateFormat(FILENAME_FORMAT).format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(outputDirectory, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${e.message}", e)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = photoFile.absolutePath
                    Toast.makeText(baseContext, "拍照成功", Toast.LENGTH_SHORT).show()
                    var intent = Intent(this@CameraActivity, ProcessActivity::class.java)
                    intent.putExtra("uri", savedUri.toString())
                    intent.putExtra("imgId", "carema")
                    //传入分数
                    var tempPoint = text_22.text
                    intent.putExtra("score", tempPoint)
                    startActivity(intent)
                    finish()
                }
            })
    }

    // 获取externalMediaDirs中的路径
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }


    // 监听返回的授权情况
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "您没有给予相关权限.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // 分析
    private inner class DataAnalyzer(private val listener: AnalyseListener) :
        ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(image: ImageProxy) {
            if (image.image != null) {
                var scoreList = ArrayList<Float>()
                var bitmap = BitmapTools.toBitmap(image.image!!)
                val degrees = image.imageInfo.rotationDegrees
                bitmap = when (degrees) {
                    90 -> BitmapTools.rotate(bitmap, 90)
                    180 -> BitmapTools.rotate(bitmap, 180)
                    270 -> BitmapTools.rotate(bitmap, 270)
                    else -> BitmapTools.rotate(bitmap, 0)
                }
                if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    bitmap = BitmapTools.flipHorizontal(bitmap)!!
                }

                if (pose_cb.isChecked) {
                    val keyPointList = poseNet.process(bitmap)
                    poseBitmap.eraseColor(Color.argb(0, 0, 0, 0))
                    if (degrees == 270) {
                        listener.onKeyPointFinish(keyPointList)
                    } else {
                        runOnUiThread {
                            pose_iv.setImageBitmap(poseBitmap)
                        }
                    }

                } else {
                    var allBitmap = BitmapTools.get9Image(bitmap)
                    if (model.startCode != 1) {
                        for (bitmapPart in allBitmap) {
                            var score = model.process(bitmapPart)
                            val s1 = MetricsUtil.getSMD2(bitmapPart) * 0.1
                            val s2 = MetricsUtil.getSNR(bitmapPart) * 0.1
                            scoreList.add((score + s1 + s2).toFloat())
                        }
                        sleep(400)
                        listener.onScoreFinish(scoreList)
                    } else {
                        listener.onScoreFail()
                    }
                }
            }
            image.close()
        }
    }

    fun getWholeImage(keyPointList: ArrayList<KeyPoint>, width: Int, height: Int) {
        val canvas = Canvas(poseBitmap)
        val paint = Paint()
        paint.color = Color.rgb(0x4F, 0xC3, 0xF7)
        paint.strokeWidth = 5F
        for (keyPointGroup in PoseNet.keyPointGroups) {
            val keyPoint0 = keyPointList[keyPointGroup[0]]
            val keyPoint1 = keyPointList[keyPointGroup[1]]
            if (keyPoint0.conf > PoseNet.threshold && keyPoint1.conf > PoseNet.threshold)
                canvas.drawLine(
                    keyPoint0.x * width / 257,
                    keyPoint0.y * height / 257,
                    keyPoint1.x * width / 257,
                    keyPoint1.y * height / 257,
                    paint
                )
        }
        paint.color = Color.rgb(0xFF, 0x8A, 0x65)
        for (keyPoint in keyPointList) {
            if (keyPoint.conf > PoseNet.threshold) {
                canvas.drawCircle(
                    keyPoint.x * width / 257,
                    keyPoint.y * height / 257,
                    8F,
                    paint
                )
            }
        }
    }
}
