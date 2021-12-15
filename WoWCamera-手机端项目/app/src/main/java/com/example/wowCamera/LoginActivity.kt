package com.example.wowCamera

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wowCamera.utils.CountDownTimerUtils
import com.example.wowCamera.utils.HttpUtils
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.lang.Exception
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
    private var captchaCode: String? = null
    private var phoneNumber = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val countDownTimerUtils = CountDownTimerUtils(getCodeTextView, 30000, 1000)
        //网络通信
        if (Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        getVerifyCode()

        phoneEdit.setOnClickListener {
//            phoneEdit.textColors = "@color/blue_login_btn";
            phoneEdit.setTextColor(getResources().getColor(R.color.blue_login_btn))
        }
        captchaEdit.setOnClickListener {
            captchaEdit.setTextColor(getResources().getColor(R.color.blue_login_btn))
        }
        codeEdit.setOnClickListener {
            codeEdit.setTextColor(getResources().getColor(R.color.blue_login_btn))
//            codeEdit.drawableleft
        }

        captchaCard.setOnClickListener {
            getVerifyCode()
        }
        getCodeTextView.setOnClickListener {
            val captcha = captchaEdit.text
            val phone = phoneEdit.text
            if (captcha.toString().toLowerCase() != captchaCode) {
                Toast.makeText(this, "图形验证码有误", Toast.LENGTH_SHORT).show()
                getVerifyCode()
            } else if (phone.length != 11) {
                Toast.makeText(this, "手机号格式有误", Toast.LENGTH_SHORT).show()
            } else {
                countDownTimerUtils.start()
                thread {
                    Looper.prepare()
                    phoneNumber = phone.toString()
                    try {
                        val verifyImageAddress = "http://82.156.49.202:8080/user/getSMSVerifyCode/$phoneNumber"
                        val verifyImageResponse = HttpUtils.sendHttpRequest(verifyImageAddress)

                        var resultCode = verifyImageResponse?.string()
                        Log.d("xdy", "resultCode:$resultCode")
                        if (resultCode=="200") {
                            Toast.makeText(this, "验证码请求成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "验证码请求失败", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "验证码请求失败,请检查网络情况", Toast.LENGTH_SHORT).show()
                    }
                    Looper.loop()
                }
            }
        }
        confirm_button.setOnClickListener {
            try {
                val smsCode = codeEdit.text
                val loginAddress = "http://82.156.49.202:8080/user/login/$phoneNumber/$smsCode"
                val loginResponse = HttpUtils.sendHttpRequest(loginAddress)
                var strResult = loginResponse?.string()
                Log.d("xdy", strResult.toString())
                if (strResult=="") {
                    Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show()
                } else {
                    val result = JSONObject(strResult)
                    val token = result.get("id")
                    val name = result.get("name")
                    Log.d("xdy", token.toString())
                    val intent = Intent()
                    intent.putExtra("token", token.toString())
                    intent.putExtra("name", name.toString())
                    setResult(RESULT_OK, intent)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "登录失败, 请检查网络连接", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun getVerifyCode(){
        try {
            //获取验证码图片
            val verifyImageAddress = "http://82.156.49.202:8080/file/getVerifyCodeImage"
            val verifyImageResponse = HttpUtils.sendHttpRequest(verifyImageAddress)
            //获取验证码文本
            val verifyTextAddress = "http://82.156.49.202:8080/file/getVerifyCodeText"
            val verifyTextResponse = HttpUtils.sendHttpRequest(verifyTextAddress)
            //解析
            val imageBytes = verifyImageResponse?.bytes()
            if(imageBytes != null){
                val verifyBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (imageBytes != null) {
                    Log.d("xdy", imageBytes.size.toString())
                }
                if (verifyTextResponse != null) {
                    captchaCode = verifyTextResponse.string().toLowerCase()
                }

                val drawable = BitmapDrawable(this.resources, verifyBitmap)
                captchaImageView.background = drawable
                Log.d("test", captchaCode.toString())
            }

        } catch (e: Exception){
            Toast.makeText(this, "获取验证码图片失败，请检查网络状态", Toast.LENGTH_SHORT).show()
        }
    }


}