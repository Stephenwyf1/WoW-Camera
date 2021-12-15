package com.wyj.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhenzi.sms.ZhenziSmsClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SendSms {

    public static void main(String[] args) {
        SendSms.send("13032861661");
    }


    private static final long serialVersionUID = 1L;
    //短信平台相关参数
    private static String apiUrl = "https://sms_developer.zhenzikj.com";
    private static String appId = "107675";
    private static String appSecret = "a536c3a4-8849-42d2-a98d-f36a00975880";

    /**
     * 短信平台使用的是榛子云短信(smsow.zhenzikj.com)
     */
    public static String send(String phoneNumber){
        String verifyCode = null;
        try {
            JSONObject json = null;
            //生成6位验证码
            verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);
            //发送短信
            ZhenziSmsClient client = new ZhenziSmsClient(apiUrl, appId, appSecret);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("number", phoneNumber);
            params.put("templateId", "3009");
            String[] templateParams = new String[2];
            templateParams[0] = verifyCode;
            templateParams[1] = "5分钟";
            params.put("templateParams", templateParams);
            String result = client.send(params);

            json = JSONObject.parseObject(result);
//            System.out.println(json);
            if(json.getIntValue("code") != 0){//发送短信失败
//                renderData(response, "fail");
                System.out.println("发送短信失败");
                return null;
            }
            //将验证码存到session中,同时存入创建时间
            //以json存放，这里使用的是阿里的fastjson
//            HttpSession session = request.getSession();
//            json = new JSONObject();
//            json.put("mobile", mobile);
//            json.put("verifyCode", verifyCode);
//            json.put("createTime", System.currentTimeMillis());
//            // 将认证码存入SESSION
//            request.getSession().setAttribute("verifyCode", json);
//            renderData(response, "success");
//            return ;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        renderData(response, "fail");
        return verifyCode;
    }

//    protected void renderData(HttpServletResponse response, String data){
//        try {
//            response.setContentType("text/plain;charset=UTF-8");
//            response.getWriter().write(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}


