package com.wyj.controller;

import com.wyj.service.FileService;
import com.wyj.utils.VerifyCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/file")
public class FileController {

    @Value("${fileUploadLocation}")
    private String fileUploadLocation;
    @Value("${fileReceiveLocation}")
    private String fileReceiveLocation;
    @Autowired
    private FileService fileService;

    private VerifyCode verifyCode; //图形验证码

    @RequestMapping("/upload")
    public String upload(){
        return "upload";
    }

    /**
     * 单文件上传保存到服务器
     * @param name 前端传的文件名
     * @param uploadFile 安卓/前端上传的文件
     * @throws IOException
     */
    @RequestMapping("/uploadFile")
    @ResponseBody
    public void uploadFile(String name, @RequestParam("file") MultipartFile uploadFile) throws IOException {
        if (name != null){
            System.out.println(name);
        }

        if (uploadFile != null){
            String originalFilename = uploadFile.getOriginalFilename();
            String filename = fileUploadLocation+originalFilename;

            fileService.uploadFile(uploadFile, filename);

            log.info("上传文件结束，文件名为：" + originalFilename);
        }else {
            log.info("上传文件为空！");
        }

    }

    /**
     * 单文件上传保存，且给安卓/前端返回图片
     * @param name 前端传的文件名
     * @param uploadFile 安卓/前端上传的文件
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/uploadFile_return", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public BufferedImage uploadFileAndReturnResult(String name, @RequestParam("file") MultipartFile uploadFile) throws IOException {
        if (name != null){
            System.out.println(name);
        }

        if (uploadFile != null){
            String originalFilename = uploadFile.getOriginalFilename();
            String filename = fileUploadLocation+originalFilename;

            fileService.uploadFile(uploadFile, filename);

            log.info("上传文件结束，文件名为：" + originalFilename);
        }else {
            log.info("上传文件为空！");
        }

        try (InputStream is = new FileInputStream(fileReceiveLocation+"test.jpeg")){
            return ImageIO.read(is);
        }
    }

    /**
     * 单文件上传保存，且调用神经网络模型
     * @param uploadFile 安卓/前端上传的文件
     * @param type 指定神经网络类型，目前有classification,image(测试，是python返回给java图片)
     * @throws IOException
     */
    @RequestMapping("/uploadFile/{type}")
    @ResponseBody
    public void uploadFileToModel(@RequestParam("file") MultipartFile uploadFile, @PathVariable String type) throws IOException {
        if (uploadFile != null){
            String originalFilename = uploadFile.getOriginalFilename();
            String filename = fileUploadLocation+originalFilename;

            fileService.uploadFileToModel(uploadFile, filename, type);

            log.info("上传文件结束，文件名为：" + originalFilename+",调用的神经网络类型为:"+type);
        }else {
            log.info("上传文件为空！");
        }

    }

    /**
     * 单文件上传保存，且调用相关模型，给安卓/前端返回图片
     * @param uploadFile 安卓/前端上传的文件
     * @param type 指定神经网络类型，目前有classification,image(测试，是python返回给java图片)
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/process/{type}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public BufferedImage process(@RequestParam("file") MultipartFile uploadFile, @PathVariable String type) throws IOException {

        if (uploadFile != null){
            String originalFilename = uploadFile.getOriginalFilename();
            String filename = fileUploadLocation+originalFilename;

            fileService.uploadFileToModel(uploadFile, filename, type);

            log.info("上传文件结束，文件名为：" + originalFilename+",调用的神经网络类型为:"+type);
        }else {
            log.info("上传文件为空！");
        }

        try (InputStream is = new FileInputStream(fileReceiveLocation+type+"_result.jpg")){
            return ImageIO.read(is);
        }
    }

    /**
     * AI诗人
     * @param uploadFile 安卓/前端上传的文件
     * @return 诗句
     * @throws IOException
     */
    @RequestMapping(value = "/process/poem")
    @ResponseBody
    public String process(@RequestParam("file") MultipartFile uploadFile) throws IOException {

        String poem = "";

        if (uploadFile != null){
            String originalFilename = uploadFile.getOriginalFilename();
            String filename = fileUploadLocation+originalFilename;

            poem = fileService.writePoem(uploadFile, filename);

            log.info("该图片的诗句:" + poem);
        }else {
            log.info("上传文件为空！");
        }

        return poem;

    }

//    @RequestMapping("/getVerifyCode")
//    @ResponseBody
//    public List<Object> getVerifyCode(){
//        VerifyCode verifyCode = new VerifyCode();
////        this.verifyCode = verifyCode;
//        BufferedImage image = verifyCode.getImage();
//        String text = verifyCode.getText();
//        List<Object> response = new ArrayList<Object>();
//        response.add(image);
//        response.add(text);
//        System.out.println(response);
//        return response;
//    }

    /**
     * 获得图形验证码的图片，每次访问都重新随机生成
     * @return 图形验证码的图片
     */
    @RequestMapping(value = "/getVerifyCodeImage", produces = {MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public BufferedImage getVerifyCodeImage(){
        VerifyCode verifyCode = new VerifyCode();
        this.verifyCode = verifyCode;
        BufferedImage image = this.verifyCode.getImage();
        log.info("成功获取图片验证码！");
//        String str = verifyCode.getText();
        return image;
    }

    /**
     * 获得图形验证码的文本，需要先调用 获得图形验证码的图片，才能调用此函数
     * @return 图形验证码的文本
     */
    @RequestMapping("/getVerifyCodeText")
    @ResponseBody
    public String getVerifyCodeText(){
        String text = null;
        if (this.verifyCode != null){
            text = this.verifyCode.getText();
        }
        return text;
    }


    //    //多文件上传
    //    @RequestMapping("/uploadFiles")
    //    @ResponseBody
    //    public void uploadFiles(String name, MultipartFile[] uploadFiles) throws IOException {
    //        System.out.println(name);
    //        for (MultipartFile uploadFile : uploadFiles) {
    //            String originalFilename = uploadFile.getOriginalFilename();
    //            String filename = fileUploadLocation+originalFilename;
    //            //保存文件
    //            uploadFile.transferTo(new File(filename));
    ////            //将上传的图片发送给python脚本处理
    ////            InvokePython.pythonResolve(filename);
    //        }
    //
    //    }

}
