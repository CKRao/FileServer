package com.clark.fileserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @Author: ClarkRao
 * @Date: 2018/12/1 16:10
 * @Description: 文件上传的处理器
 */
@RestController
public class FileController {
    public static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    /**
     * 文件存放路径
     */
    @Value("${clark.filePath}")
    private String filePath;

    /**
     * 单个文件上传
     * @param file 上传的文件
     * @return
     */
    @RequestMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "文件为空";
            }
            //获取文件名
            final String fileName = file.getOriginalFilename();
            LOGGER.info("上传的文件名为：" + fileName);
            //获取文件后缀名
            final String suffixName = fileName.substring(fileName.lastIndexOf("."));
            LOGGER.info("文件的后缀名为：" + suffixName);
            //设置文件存储路径
            String path = filePath + fileName;
            File upload = new File(path);
            //检测是否存在目录
            if (!upload.getParentFile().exists()) {
                //新建目录
                upload.getParentFile().mkdirs();
            }
            //写入文件
            file.transferTo(upload);
            return "上传成功";
        }catch (IllegalStateException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "上传失败";
    }

    /**
     * 多文件上传
     * @param request
     * @return
     */
    @PostMapping("/batch")
    public String handleFileUpload(HttpServletRequest request) {
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");

        MultipartFile file = null;
        BufferedOutputStream stream = null;

        int fileSize = files.size();
        for (int i = 0; i < fileSize; i++) {
            file = files.get(i);
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    //设置文件路径以及名称
                    stream = new BufferedOutputStream(new FileOutputStream(
                            new File(filePath + file.getOriginalFilename())));
                    stream.write(bytes);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    stream = null;
                    return "第 " + i + " 个文件上传失败 ==> "
                            + e.getMessage();
                }
            }else {
                return "第 " + i + " 个文件上传失败，因为该文件为空";
            }
        }
        return "上传成功";
    }

}
