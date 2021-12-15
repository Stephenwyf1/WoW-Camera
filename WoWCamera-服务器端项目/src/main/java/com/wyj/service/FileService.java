package com.wyj.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    void uploadFile(MultipartFile uploadFile, String filename) throws IOException;

    void uploadFileToModel(MultipartFile uploadFile, String filename, String type) throws IOException;

    String writePoem(MultipartFile uploadFile, String filename) throws IOException;
}
