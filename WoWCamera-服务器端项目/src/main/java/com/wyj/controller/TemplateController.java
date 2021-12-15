package com.wyj.controller;

import com.wyj.domain.Template;
import com.wyj.service.TemplateService;
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
import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("/template")
public class TemplateController {

    @Value("${fileUploadLocation}")
    private String fileUploadLocation;

    @Autowired
    private TemplateService templateService;

    @RequestMapping("/saveTemplate/{id}/{name}")
    @ResponseBody
    public String saveTemplate(@RequestParam("file") MultipartFile uploadFile,
                                @PathVariable("id") Long id,
                                @PathVariable("name") String name){
        Template template = new Template();
        template.setUserId(id);
        template.setName(name);
        String uuid = UUID.randomUUID().toString().replace("-","");
        template.setUuid(uuid);
        String parentPath = fileUploadLocation + id;
        String uri = parentPath + "/" + name + ".jpg";
        template.setImg_uri(uri);
        Boolean update_file = false;
        try {
            File file = new File(uri);
            if (file.exists()){
                update_file = true;
            }else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()){
                    parentFile.mkdirs();
                }
            }
            uploadFile.transferTo(file);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("滤镜保存失败");
            return null;
        }
        if (update_file){
            Template t = templateService.getTemplateByNameAndUserId(name, id);
            t.setUuid(uuid);
            templateService.updateTemplate(t);
        }else {
            templateService.saveTemplate(template);
        }
        log.info("滤镜保存成功，该滤镜的分享字符串为"+uuid);
        return uuid;
    }


    @RequestMapping(value = "/getTemplate/{uuid}",produces = {MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public BufferedImage getTemplateByUUID(@PathVariable("uuid") String uuid) throws IOException {

        Template template = templateService.getTemplateByUUID(uuid);
        if (template == null){
            log.info("未找到该滤镜！");
            return null;
        }

        log.info("成功获取"+uuid+"的滤镜图片");

        String uri = template.getImg_uri();
        try (InputStream is = new FileInputStream(uri)){
            return ImageIO.read(is);
        }
    }
}
