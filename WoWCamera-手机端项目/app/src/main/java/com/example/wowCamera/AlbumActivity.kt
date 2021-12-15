package com.example.wowCamera

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.contentValuesOf
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.wowCamera.adapters.OrdersPagerAdapter
import com.example.wowCamera.utils.AiUnitAes
import com.example.wowCamera.utils.BitmapTools
import com.example.wowCamera.utils.MyDatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.listitem.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AlbumActivity : BaseActivity() {
    var ImgUrl = ArrayList<String?>()
    var folders = ArrayList<String?>()
    lateinit var aesModel: AiUnitAes
    lateinit var viewPager2: ViewPager2
    lateinit var outputDir : File
    var folderName = "x"//文件夹名字
    var folderId = "x"//文件夹ID————用于确认照片分布在哪一个文件夹
    var scoreList = ArrayList<String>()//文件夹的照片分数
    var scoreListOneImage = ArrayList<String>()
    var SQL_VERSION = 3//数据库版本
    val ALBUM_ID = "albumid"
    var createTime = ""
    override fun onDestroy() {
        super.onDestroy()
        aesModel.close()
    }

    override fun onResume() {
        super.onResume()
        aesModel = AiUnitAes(this.applicationContext)
        aesModel.connect()
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
        initFragment(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        //写数据
        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
        dbHelper.writableDatabase
        contentValuesOf("" to "")
        initSql(dbHelper)
//        initSqlAlbum(dbHelper)
//        initSqlFolder(dbHelper)

        outputDir = getOutputDirectory()
        viewPager2 = findViewById(R.id.viewPager)
        viewPager2.adapter = OrdersPagerAdapter(this)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val tabLayoutMediator = TabLayoutMediator(
            tabLayout,
            viewPager2,
            TabConfigurationStrategy { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "照片"
                        tab.setIcon(R.drawable.img_selector)
//                        tab.setIcon(R.drawable.ic_baseline_image_24)
                    }
                    1 -> {
                        tab.text = "文件夹"
                        tab.setIcon(R.drawable.folder_selector)
//                        tab.setIcon(R.drawable.ic_baseline_folder_24)
                    }
                }
            })

        tabLayoutMediator.attach()
        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })


        val addPhotos = findViewById<FloatingActionButton>(R.id.addPhotos)
        addPhotos.setOnClickListener {
            val popup = PopupMenu(this@AlbumActivity, addPhotos)
            popup.menuInflater.inflate(R.menu.ai_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.addPhotoBtn -> {
                        Log.d(TAG, "点击添加照片")
                        getOneImageUri()
                    }
                    R.id.addFolderBtn -> {
                        val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm")
                        val simpleDateFormat2 = SimpleDateFormat("yyMMdd_HHmmssSS")
                        val simpleDateFormat3 = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        val date = Date(System.currentTimeMillis())
                        folderName = simpleDateFormat.format(date)
                        folderId = simpleDateFormat2.format(date)
                        createTime = simpleDateFormat3.format(date)

                        folders.add(folderName)
                        getImageUri()
                    }

                    R.id.delPhoto -> {

                        ImgUrl.clear()
                        scoreListOneImage.clear()
                        delPhotoSql(dbHelper)
//                        saveData()
                        viewPager2.currentItem = 0
                    }
                    R.id.delFolder -> {
                        folders.clear()
                        scoreList.clear()
                        delFolderSql(dbHelper)
//                        saveData()
                        viewPager2.currentItem = 1
                    }
                }
                true
            }
            popup.show()
        }

        // GuideView
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRunAlbum", true)
        if(isFirstRun) {
//            Toast.makeText(this, "第一次运行", Toast.LENGTH_SHORT).show()
            sharedPreferences.edit().putBoolean("isFirstRunAlbum", false).apply()
            lateinit var guideView1: GuideView
            lateinit var guideView2: GuideView

            val tv = TextView(this)
            tv.text = "欢迎进入工作室，左右滑动以切换管理视图"
            tv.setTextColor(resources.getColor(R.color.white))
            tv.textSize = 14f
            tv.gravity = Gravity.CENTER

            guideView1 = GuideView.Builder
                .newInstance(this)
                .setTargetView(tabLayout)
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.BOTTOM)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置椭圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .setRadius(-10)
                .build()

            guideView1.setOnclickListener {
                guideView1.hide()
                tv.text = "按下加号进行图片管理"
                tv.textSize = 13f
                guideView2.show()
            }

            guideView2 = GuideView.Builder
                .newInstance(this)
                .setTargetView(addPhotos) //设置目标
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.LEFT)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView2.setOnclickListener {
                guideView2.hide()
            }

            guideView1.show()
        }
    }
    private fun getImageUri() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE)
    }
    private fun getOneImageUri() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_ONE_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            PICK_ONE_IMAGE_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    var imageUri = data.data
                    if (imageUri != null) {
                        val address = imageUri.toString().split("/")
                        val fileName = address[address.lastIndex] + ".jpg"
                        val bitmap =
                            BitmapTools.getBitmapFromUriStream(this.applicationContext, imageUri)
                        val file = File(outputDir, fileName)
                        val fos = FileOutputStream(file)
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        fos.flush()
                        fos.close()
                        val newImageUri = file.absolutePath
                        if (ImgUrl.contains(newImageUri)) {
                            Toast.makeText(this, "已经添加过此图", Toast.LENGTH_SHORT).show()
                        } else {
                            var score = aesModel.process(bitmap)
                            score = changePoint(score.toDouble()).toFloat()
                            var scoreStr = String.format("%.2f", score)
                            val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
                            addPhotosql(
                                dbHelper,
                                newImageUri,
                                String.format("%.2f", score),
                                ALBUM_ID
                            )

                            if (scoreStr.equals("-40.00")){
                                Toast.makeText(this,"未打开AIUnit，无法获取照片分数",Toast.LENGTH_SHORT).show()
                            }
                            viewPager2.adapter = OrdersPagerAdapter(this)
                        }
                        viewPager2.currentItem = 0
                    }
                }
            }
            PICK_IMAGES_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data!!.clipData != null) {
                        val count = data.clipData!!.itemCount
//                        var imgData = ArrayList<ImgUriAndScore>()
                        var folderImgDataUrl = ArrayList<String>()
                        var folderImgDataScore = ArrayList<Float>()

                        for (i in 0 until count) {
                            var imageUri = data.clipData!!.getItemAt(i).uri
                            val address = imageUri.toString().split("/")
                            val fileName = address[address.lastIndex] + ".jpg"
                            Log.d("xdy", "filename$fileName")
                            Log.d("xdy", "imgUri$imageUri")
                            val bitmap = BitmapTools.getBitmapFromUriStream(
                                this.applicationContext,
                                imageUri
                            )
                            val file = File(this.outputDir, fileName)
                            val fos = FileOutputStream(file)
                            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            fos.flush()
                            fos.close()
                            var newImageUri = file.absolutePath
                            var score = aesModel.process(bitmap)
                            Log.d(TAG, "score:$score")
                            folderImgDataUrl.add(newImageUri)
                            folderImgDataScore.add(score)
                        }

                        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
                        createF(dbHelper, folderName, folderId,createTime)
                        for (i in 0 until folderImgDataUrl.size){
                            val score = changePoint(folderImgDataScore[i].toDouble()).toFloat()
                            var scoreStr = String.format("%.2f", score)
//                            (dbHelper,folderId,folderImgDataUrl[i],scoreStr)
                            addPhotosql(dbHelper,folderImgDataUrl[i],scoreStr,folderId)
                        }
                        initFragment(1)

                        viewPager2.currentItem = 1
                    }
                }
            }
        }
    }
    open fun getOutputDirectory(): File {

        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }
    inner class ImgUriAndScore(val uri: String, val score: Float) : Comparable<ImgUriAndScore>{
        override fun compareTo(other: ImgUriAndScore): Int {
            return this.score!!.compareTo(other.score!!)
        }
    }



    private fun fromUriToBitmap(strUri: String) : Bitmap {
        var uri = Uri.parse(strUri)
        return BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
    }

    fun floatToStringArrayList(list: ArrayList<Float>):ArrayList<String?>{

        val listStr = ArrayList<String?>()
        for (i in 0 until list.size-1)
            listStr.add(list[i].toString())
        return listStr
    }

    companion object {
        private const val PICK_IMAGES_CODE = 0
        private const val PICK_ONE_IMAGE_CODE = 1
        private const val TAG = "xdy"
    }

    fun initFragment(item: Int) {
        viewPager2.adapter = OrdersPagerAdapter(this)
        viewPager2.currentItem = item
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }


    fun initSql(dbHelper: MyDatabaseHelper){
        val db = dbHelper.writableDatabase

        val cursor = db.query("Album", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                val imgurl = cursor.getString(cursor.getColumnIndex("imgurl"))
                val score = cursor.getString(cursor.getColumnIndex("score"))
                val pid = cursor.getString(cursor.getColumnIndex("id"))
                val fid = cursor.getString(cursor.getColumnIndex("fotime"))
                if(fid == ALBUM_ID){
                    ImgUrl.add(imgurl)
                    scoreListOneImage.add(score)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        val cursor2 = db.query("Folder", null, null, null, null, null, null)
        if (cursor2.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                val foldername = cursor2.getString(cursor2.getColumnIndex("foldername"))
                folders.add(foldername)
            } while (cursor2.moveToNext())

        }
        cursor2.close()
    }



    fun createF(dbHelper: MyDatabaseHelper, foldername: String, ftime: String,createTime:String){
        val db = dbHelper.writableDatabase
        contentValuesOf("" to "")
        val values = ContentValues().apply {
            put("foldername", foldername)
            put("ftime", ftime)
            put("createtime", createTime)
        }
        db.insert("Folder", null, values) // 插入数据
    }

    //删除所有的照片
    fun delPhotoSql(dbHelper: MyDatabaseHelper){
        val db = dbHelper.writableDatabase
        db.execSQL("delete from Album where fotime = ?", arrayOf(ALBUM_ID))

        viewPager2!!.adapter = OrdersPagerAdapter(this)

    }
    //删除所有的文件夹
    fun delFolderSql(dbHelper: MyDatabaseHelper){
        val db = dbHelper.writableDatabase
        db.execSQL("delete from Folder")
        db.execSQL("delete from Album where fotime != ?", arrayOf(ALBUM_ID))
        viewPager2!!.adapter = OrdersPagerAdapter(this)

    }

    fun delImgUrlbyPosition(position:Int){
        ImgUrl.removeAt(position)
    }

}