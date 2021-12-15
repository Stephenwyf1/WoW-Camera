package com.wyj.controller;

import com.wyj.domain.User;
import com.wyj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //安卓前端每次request为不同的请求，不是同一个session，不能用session来保存验证码
    private Map<String,String> verifyCodeMap = new HashMap<String,String>();


    @RequestMapping("/login/{phoneNumber}/{SMSCode}")
    @ResponseBody
    public User login(@PathVariable String phoneNumber, @PathVariable String SMSCode){
        if (phoneNumber != null){

//            String sessionSMSCode = verifyCodeMap.get(phoneNumber);
//
//            //System.out.println("发送的验证码"+sessionSMSCode);
//            //System.out.println("用户的验证码"+SMSCode);
//            if (!sessionSMSCode.equals(SMSCode)){
//                return null;
//            }

            User user = new User();
            String uuid = UUID.randomUUID().toString();
            String name = uuid.substring(0,8);
            user.setName("用户"+name);
            user.setPhoneNumber(phoneNumber);
            User loginUser = userService.login(user);
            log.info("登录成功！用户："+loginUser);
            return loginUser;
        }else {
            return null;
        }
    }

    @GetMapping("/query/{id}")
    @ResponseBody
    public User queryById(@PathVariable Long id){
        User user = userService.queryById(id);

        return user;
    }

//    @GetMapping("/query/{id}")
//    public ModelAndView queryById(@PathVariable Long id){
//        ModelAndView modelAndView = new ModelAndView();
//        User user = userService.queryById(id);
//
//        modelAndView.addObject("user",user);
//        modelAndView.setViewName("user");
//        return modelAndView;
//    }

    /**
     * 发送短信验证码
     * @return 短信验证码
     */
    @RequestMapping("/getSMSVerifyCode/{phoneNumber}")
    @ResponseBody
    public int getSMSVerifyCode(@PathVariable String phoneNumber){

        int code = 200;
        log.info("即将给"+phoneNumber+"发送验证码");
        String SMSCode = userService.sendSMS(phoneNumber);
        if (SMSCode == null){
            code = 500;
            log.info("发送验证码失败");
            return code;
        }
        log.info("发送成功");
        verifyCodeMap.put(phoneNumber,SMSCode);
        return code;
    }


    /*********************以下为本地测试函数**************************************/

    @GetMapping("/queryAll")
    public ModelAndView queryAll(){
        ModelAndView modelAndView = new ModelAndView();
        List<User> userList = userService.queryAll();

        modelAndView.addObject("userList",userList);
        modelAndView.setViewName("userList");
        return modelAndView;
    }

    @RequestMapping("/saveUI")
    public String saveUI(){
        return "save";
    }

    @RequestMapping("/save")
    public String save(User user){
        userService.save(user);
        return "redirect:/user/queryAll";
    }

    @RequestMapping("/test1/{userId}")
    @ResponseBody
    public List<Object> test(@PathVariable String userId){
//        userService.save(user);
        System.out.println(userId);
//        System.out.println(passwd);
        List<Object> response = new ArrayList<Object>();
        User user = new User();
        user.setPhoneNumber("123456");
        response.add(user);
        response.add(123456);
        return response;
    }


}
