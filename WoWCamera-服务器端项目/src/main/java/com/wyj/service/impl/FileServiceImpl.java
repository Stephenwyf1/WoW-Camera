package com.wyj.service.impl;

import com.wyj.service.FileService;
import com.wyj.utils.InvokePython;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service("fileService")
public class FileServiceImpl implements FileService {

    @Autowired
    private InvokePython invokePython;

    @Override
    public void uploadFile(MultipartFile uploadFile, String filename) throws IOException {
        //保存文件
        uploadFile.transferTo(new File(filename));
    }

    @Override
    public void uploadFileToModel(MultipartFile uploadFile, String filename, String type) throws IOException {
        //保存文件
        uploadFile.transferTo(new File(filename));
//        System.out.println(type);
//        System.out.println(type.equals("denoise"));
//        if (type.equals("denoise")){
//            invokePython.denoise(filename);
//        }else {
            //将上传的图片发送给python脚本处理
            invokePython.pythonResolve(filename, type);
//        }

    }

    @Override
    public String writePoem(MultipartFile uploadFile, String filename) throws IOException {
        //保存文件
        uploadFile.transferTo(new File(filename));
        //将上传的图片发送给python脚本处理
        String poem = invokePython.pythonResolve(filename, "poem");

        poem = poem.replace("，。，。，。，。，。，。","");
        poem = poem.replace("。","。\n");
//        System.out.println(poem);
        return poem;
    }
}
