package com.example.wowCamera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity" // 调试TAG
        private const val REQUEST_CODE_LOGIN = 1
        private const val SELECT_WALLPAPER = 2
        lateinit var outputDir : File
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) // 透明任务栏
        setContentView(R.layout.activity_main)

        val animation = AnimationUtils.loadAnimation(this, R.anim.move_anim)
        bg_layout.startAnimation(animation)


        val wallpaperFileName =
            getOutputDirectory().absolutePath + "/" + "user_wallpaper" + ".jpg"


        if(fileIsExists(wallpaperFileName)){
//            Log.d(TAG, "onCreate: ")
//            val bitmap = BitmapFactory.decodeFile(wallpaperFileName)
            val bitmap = BitmapFactory.decodeFile(wallpaperFileName, getBitmapOption(2))
            wallpaper_iv.setImageBitmap(bitmap)
            Log.d("qxy", "onCreate: $wallpaperFileName")
        }

        when(getToken()) {
            "" -> {
                showLogoutState()
            }
            else -> {
                showUserData(getUserName())
            }
        }
        logoutBtn.setOnClickListener {
            showLogoutState()
            Toast.makeText(this, "已注销", Toast.LENGTH_SHORT).show()
        }
        mainMenu.setOnClickListener {
            drawer.openDrawer(GravityCompat.END) // 打开抽屉
        }
        cameraBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        login_btn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_LOGIN)
        }
        albumBtn.setOnClickListener{

            val intent = Intent()
            intent.setClass(this, AlbumActivity::class.java)
            startActivity(intent)
        }
        change_wall_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_WALLPAPER)
        }
        default_wall_btn.setOnClickListener {
            wallpaper_iv.setImageResource(R.drawable.bg)
        }
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)

        // GuideView
        val isGuideViewOn = sharedPreferences.getBoolean("isGuideViewOn", true)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRunMain", true)
        if(isFirstRun) {
            showGuideView()
            sharedPreferences.edit().putBoolean("isFirstRunMain", false).apply()
        }

        see_guide_btn.setOnClickListener {
            drawer.closeDrawer(GravityCompat.END) // 打开抽屉
            showGuideView()
//            sharedPreferences.edit().putBoolean("isFirstRunMain", true).apply()
            sharedPreferences.edit().putBoolean("isFirstRunAlbum", true).apply()
            sharedPreferences.edit().putBoolean("isFirstRunCamera", true).apply()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_LOGIN -> if (resultCode == RESULT_OK) {
                val token = data?.getStringExtra("token")
                val name = data?.getStringExtra("name")
                saveUserData(token, name)
                showUserData(name)
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
            }
            SELECT_WALLPAPER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    var imageUri = data.data
                    if (imageUri != null) {
                        val bitmap = getBitmapFromUri(imageUri)

                        val fileName =
                            getOutputDirectory().absolutePath + "/" + "user_wallpaper" + ".jpg"
                        val file = File(fileName)
                        val fos = FileOutputStream(file)
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        fos.flush()
                        fos.close()

                        wallpaper_iv.setImageURI(imageUri)
                    }
                }
            }
        }
    }

    private fun showGuideView(){

            lateinit var guideView1: GuideView
            lateinit var guideView2: GuideView
            lateinit var guideView3: GuideView
            lateinit var guideView4: GuideView
            val tv = TextView(this)
            tv.text = "欢迎使用WoWCamera!"
            tv.setTextColor(resources.getColor(R.color.white))
            tv.textSize = 20f
            tv.gravity = Gravity.CENTER

            guideView1 = GuideView.Builder
                .newInstance(this)
                .setTargetView(textView3)
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.BOTTOM)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置椭圆形显示区域，
                .setBgColor(resources.getColor(R.color.transparent))
                .setRadius(-10)
                .build()

            guideView1.setOnclickListener {
                guideView1.hide()
                tv.text="按下此按钮可进入相机进行拍摄"
                tv.textSize = 13f
                guideView2.show()
            }

            guideView2 = GuideView.Builder
                .newInstance(this)
                .setTargetView(cameraBtn) //设置目标
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.RIGHT_TOP)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView2.setOnclickListener {
                guideView2.hide()
                tv.text = "按下此按钮可以进入工作室"
                guideView3.show()
            }

            guideView3 = GuideView.Builder
                .newInstance(this)
                .setTargetView(albumBtn) //设置目标tv
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.LEFT_TOP)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView3.setOnclickListener {
                guideView3.hide()
                tv.text = "按下此按钮进行个人信息登录哦"
                guideView4.show()
            }

            guideView4 = GuideView.Builder
                .newInstance(this)
                .setTargetView(mainMenu) //设置目标
                .setCustomGuideView(tv)
                .setDirction(GuideView.Direction.LEFT_BOTTOM)
                .setShape(GuideView.MyShape.CIRCULAR) // 设置圆形显示区域，
                .setBgColor(resources.getColor(R.color.shadow))
                .build()

            guideView4.setOnclickListener {
//                Toast.makeText(this@MainActivity, "引导结束", Toast.LENGTH_SHORT).show()
                guideView4.hide()
            }
            guideView1.show()

    }
    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }
    fun fileIsExists(strFile: String?): Boolean {
        try {
            val f = File(strFile)
            if (!f.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }
    private fun getBitmapOption(inSampleSize: Int): BitmapFactory.Options? {
        System.gc()
        val options = BitmapFactory.Options()
        options.inPurgeable = true
        options.inSampleSize = inSampleSize
        return options
    }
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun getToken(): String {
        val prefs = getSharedPreferences("data", Context.MODE_PRIVATE)
        return prefs.getString("token", "") as String
    }

    private fun getUserName(): String {
        val prefs = getSharedPreferences("data", Context.MODE_PRIVATE)
        return prefs.getString("name", "") as String
    }

    private fun saveUserData(token: String?, name: String?) {
        val editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
        editor.putString("token", token)
        editor.putString("name", name)
        editor.apply()
    }

    private fun showUserData(name: String?) {
        Glide.with(this).load(R.drawable.panda).apply(RequestOptions.bitmapTransform(CircleCrop())).into(
            this.userHead
        ) // 画圆形头像
        userName.text = name
        userName.visibility = View.VISIBLE
        login_btn.visibility = View.GONE
        logoutBtn.visibility = View.VISIBLE
    }

    private fun showLogoutState(){
        Glide.with(this).load(R.drawable.user_unknown).apply(
            RequestOptions.bitmapTransform(
                CircleCrop()
            )
        ).into(this.userHead) // 画圆形头像
        login_btn.visibility = View.VISIBLE
        userName.visibility = View.GONE
        saveUserData("", "")
        logoutBtn.visibility = View.GONE
    }

}