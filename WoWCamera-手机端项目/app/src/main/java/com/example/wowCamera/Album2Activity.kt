package com.example.wowCamera

import android.app.Activity
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.wowCamera.adapters.ImgAdapter
import com.example.wowCamera.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_album2.*
import kotlinx.android.synthetic.main.activity_process.*
import kotlinx.android.synthetic.main.poem_popup_window.view.*
import kotlinx.android.synthetic.main.popup_window.view.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class Album2Activity : BaseActivity() {
    var SQL_VERSION = 3
    var folderName: String? = null

    var scoreList = ArrayList<String>()
    var ImgUrl = ArrayList<String?>()
    var shareImg = ArrayList<String?>() //被选择的照片
    var shareFile = ArrayList<File>()
    var adapter: ImgAdapter? = null
    var adapters = ArrayList<ImgAdapter>()

    var muti_select = false
    var ftime = ""
//    var size = 0
    lateinit var aesModel: AiUnitAes
    lateinit var outputDir : File

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
        initSql(ftime)
//        adapter = ImgAdapter(ImgUrl, this)
        adapter!!.setScoreList(scoreList)
//
//
        adapter!!.notifyDataSetChanged()
//        adapter!!.notifyItemInserted(adapter!!.urls.size-1)
//        Log.d("qxysql", "onRestart: "+adapter!!.getItemCount())

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album2)
        val intent = intent
        ftime = intent.getStringExtra("ftime").toString()
        val toolbar = findViewById<Toolbar>(R.id.muti_toolbar)
        val undo = findViewById<ImageView>(R.id.cancel_muti)
        val share = findViewById<ImageView>(R.id.share)
        val addPhotoInFolder = findViewById<FloatingActionButton>(R.id.addPhotoInFolder)

        outputDir = getOutputDirectory()
        //读取已有的数据
        if (ftime != null) {
            initSql(ftime)
        }
        Log.d("qxysql", "array-size-init: " + ImgUrl.size + "---" + scoreList.size)

        val textView = findViewById<TextView>(R.id.fName)
        textView.text = folderName
        val recyclerView = findViewById<RecyclerView>(R.id.RecyclerviewFps)
        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        adapter = ImgAdapter(ImgUrl, this)

        adapter!!.setScoreList(scoreList)

        recyclerView.adapter = adapter
        adapter!!.setOnItemClickListener(object : ImgAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (muti_select) { //进入多选状态
                    if (adapter!!.getChecked(position)) {
                        adapter!!.setCheck(position, false)
                        if (adapter!!.getSelectNum() == 1) {
                            aiPoem.visibility = View.VISIBLE
                            ai_poem_iv.visibility = View.VISIBLE
                        } else {
                            aiPoem.visibility = View.GONE
                            ai_poem_iv.visibility = View.GONE
                        }
                    } else {
                        adapter!!.setCheck(position, true)
                        if (adapter!!.getSelectNum() == 1) {
                            aiPoem.visibility = View.VISIBLE
                            ai_poem_iv.visibility = View.VISIBLE
                        } else {
                            aiPoem.visibility = View.GONE
                            ai_poem_iv.visibility = View.GONE
                        }
                    }
                } else {
                    val intent1 = Intent(Intent.ACTION_GET_CONTENT)
                    intent1.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    intent1.setClass(this@Album2Activity, ProcessActivity::class.java)
                    intent1.putExtra("uri", ImgUrl[position])
                    intent1.putExtra("score", scoreList[position])
                    intent1.putExtra("imgId", ftime?.let { getImgId(position, it) })


                    startActivity(intent1)

                }
            }

            override fun onItemLongClick(view: View, position: Int) {

                muti_select = true
                adapter!!.setMuti_select(muti_select)
                adapter!!.setCheck(position, muti_select)
                toolbar.visibility = View.VISIBLE
                if (adapter!!.getSelectNum() == 1) {
                    aiPoem.visibility = View.VISIBLE
                    ai_poem_iv.visibility = View.VISIBLE
                } else {
                    aiPoem.visibility = View.GONE
                    ai_poem_iv.visibility = View.GONE
                }
            }
        })
        undo.setOnClickListener {
            muti_select = false
            adapter!!.initSelectList()
            toolbar.visibility = View.GONE
            adapter!!.setMuti_select(muti_select)
//            for (i in ImgUrl.indices) {
//                adapter!!.setCheck(i, false)
//            }
            shareImg = ArrayList()
        }
        share.setOnClickListener {
            shareImg = ArrayList()
            //添加
            for (i in ImgUrl.indices) {
                if (adapter!!.getSelectList()[i]) {
                    shareImg.add(ImgUrl[i])
                }
            }

            val shareUri = ArrayList<Uri>()
            for (i in shareImg.indices) {
                val file = File(shareImg[i])
                val uri = FileProvider.getUriForFile(
                    this@Album2Activity,
                    "com.example.wowCamera.fileProvider",
                    file
                )
                shareUri.add(uri)
                shareFile.add(file)
            }
            var shareintent = Intent()
            shareintent.action = Intent.ACTION_SEND_MULTIPLE
            shareintent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUri)
            shareintent.type = "image/*"
            val string = "分享图片"
            shareintent = Intent.createChooser(shareintent, string)
            startActivity(shareintent)

        }
        delete_muti.setOnClickListener {
            //删除
//            Log.d("qxysql", "del-selectList-size: "+adapter!!.selectList.size+"--"+adapter!!.urls.size+"--"+adapter!!.selectList.size+"-ImgUrl-"+ImgUrl.size)
            for (i in adapter!!.selectList.size-1 downTo 0){
                Log.d("qxysql", "delete_muti: "+i)
                if (adapter!!.selectList[i]){
                    var  del_id = ftime?.let { it1 -> getImgId(i, it1) }
                    if (del_id != null) {
//                        scoreList.removeAt(i)
//                        ImgUrl.removeAt(i)
                        adapter!!.delSqlPhotoById(del_id)
                        adapter!!.delAdapterPhoto(i)


                    }
//                    adapter!!.delOnePhoto(i, ftime)

                }
            }
            initSql(ftime)
            adapter!!.initSelectList()
            Log.d("qxysql", "afterdel-size: "+adapter!!.selectList.size+"--"+adapter!!.urls.size+"--"+adapter!!.selectList.size+"act:"+ImgUrl.size)


            if (ImgUrl.size == 0){
                muti_select = false
                adapter!!.initSelectList()
                toolbar.visibility = View.GONE
                adapter!!.setMuti_select(muti_select)
                shareImg = ArrayList()
            }
        }
        aiPoem.setOnClickListener {
            messagevoid(2)
            for(idx in 0..adapter!!.selectList.size) {
                if(adapter!!.selectList[idx]){
                    val bitmap = BitmapFactory.decodeFile(ImgUrl[idx])
                    val smallBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
                        .copy(Bitmap.Config.ARGB_8888, true)
                    HttpUtils.sendImageForRequest(
                        this,
                        ":8080/file/process/poem",
                        smallBitmap,
                        false,
                        object : ImageListener {
                            override fun success(bitmap: Bitmap?, string: String?) {
                                runOnUiThread {
                                    dismissLoadingDialog()
                                    showPopWindow(string.toString(), 1, whole_layout)
                                }
                            }

                            override fun error(code: Int) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@Album2Activity,
                                        "AI作诗失败，请检查网络连接",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismissLoadingDialog()
                                }
                            }
                        })
                }
            }
        }

        addPhotoInFolder.setOnClickListener {
            getImageUri()
        }


    }
    companion object {
        private const val PICK_IMAGES_CODE = 0

    }
    private fun getImageUri() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            PICK_IMAGES_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        if (data!!.clipData != null) {
                            val count = data.clipData!!.itemCount
//                        var imgData = ArrayList<ImgUriAndScore>()
                            var folderImgDataUrl = ArrayList<String>()
                            var folderImgDataScore = ArrayList<Float>()

                            for (i in 0 until count) {
                                var imageUri = data.clipData!!.getItemAt(i).uri
                                val address = imageUri.toString().split("/")
                                val fileName = address[address.lastIndex] + ".jpg"
                                val bitmap = BitmapTools.getBitmapFromUriStream(
                                    this.applicationContext,
                                    imageUri
                                )
                                val file = File(outputDir, fileName)
                                val fos = FileOutputStream(file)
                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                fos.flush()
                                fos.close()
                                var newImageUri = file.absolutePath
                                var score = aesModel.process(bitmap)
                                folderImgDataUrl.add(newImageUri)
                                folderImgDataScore.add(score)
                            }

                            val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
                            var scoreStr = ""
                            for (i in 0 until folderImgDataUrl.size) {
                                val score = changePoint(folderImgDataScore[i].toDouble()).toFloat()
                                scoreStr = String.format("%.2f", score)
                                addPhotosql(dbHelper, folderImgDataUrl[i], scoreStr, ftime)
                                scoreList.add(scoreStr)
                                adapter!!.addAdapterPhoto(folderImgDataUrl[i], scoreStr)

                            }

                            if (scoreStr.equals("-40.00")){
                                Toast.makeText(this,"未打开AIUnit，无法获取照片分数",Toast.LENGTH_SHORT).show()
                            }
                            initSql(ftime)

                        }
                        else if (data != null) {
                            var imageUri = data.data
                            if (imageUri != null) {

                                val address = imageUri.toString().split("/")
                                val fileName = address[address.lastIndex] + ".jpg"
                                val bitmap =
                                    BitmapTools.getBitmapFromUriStream(
                                        this.applicationContext,
                                        imageUri
                                    )
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
                                    val dbHelper = MyDatabaseHelper(
                                        this,
                                        "wowCamera.db",
                                        SQL_VERSION
                                    )
                                    addPhotosql(dbHelper, newImageUri, scoreStr, ftime)
//                                    ImgUrl.add(newImageUri)
                                    scoreList.add(scoreStr)

//                                    adapter!!.setScoreList(scoreList)
                                    adapter!!.addAdapterPhoto(newImageUri, scoreStr)
                                    initSql(ftime)
                                    if (scoreStr.equals("-40.00")){
                                        Toast.makeText(this,"未打开AIUnit，无法获取照片分数",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    private fun initSql(ftime: String){
        scoreList = ArrayList()
        ImgUrl = ArrayList()
        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
        dbHelper.writableDatabase
        val db = dbHelper.writableDatabase

        val cursor = db.rawQuery(
            "select * from Album where fotime=? order by score desc", arrayOf(
                ftime
            )
        )
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                val imgurl = cursor.getString(cursor.getColumnIndex("imgurl"))
                val score = cursor.getString(cursor.getColumnIndex("score"))
                ImgUrl.add(imgurl)
                scoreList.add(score)
            } while (cursor.moveToNext())
        }
        cursor.close()

        var cursor_fName = db.rawQuery("select * from Folder where ftime = ?", arrayOf(ftime))
        cursor_fName.moveToFirst()
        folderName = cursor_fName.getString(cursor_fName.getColumnIndex("foldername"))
        cursor_fName.close()
    }
    fun getImgId(position: Int, ftime: String): String? {
        var id = ""
        val dbHelper = MyDatabaseHelper(this, "wowCamera.db", SQL_VERSION)
        val db: SQLiteDatabase = dbHelper.getWritableDatabase()
        val cursor = db.query(
            "Album",
            null,
            "fotime =?",
            arrayOf(ftime),
            null,
            null,
            "score desc",
            "$position,1"
        )
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                id = cursor.getString(cursor.getColumnIndex("id"))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return id
    }
    open fun getOutputDirectory(): File {

        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

}