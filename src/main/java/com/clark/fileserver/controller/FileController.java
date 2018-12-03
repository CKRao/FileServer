package com.clark.fileserver.controller;

import com.clark.fileserver.pojo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
     *
     * @param file 上传的文件
     * @return
     */
    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        Result result = new Result();
        try {
            if (file.isEmpty()) {
                result.setResult("error");
                result.setData("文件为空");
                return result;
            }
            //获取文件名
            final String fileName = file.getOriginalFilename();
            LOGGER.info("上传的文件名为：" + fileName);
//            System.out.println(fileName.lastIndexOf("."));
            //获取文件后缀名
//            final String suffixName = fileName.substring(fileName.lastIndexOf("."));
//            LOGGER.info("文件的后缀名为：" + suffixName);
            //设置文件存储路径
            String path = filePath + fileName;
            File upload = new File(path);
            //检测是否存在目录
            if (!upload.getParentFile().exists()) {
                //新建目录
                upload.getParentFile().mkdirs();
            }
            LOGGER.info("准备写入文件");
            //写入文件
            file.transferTo(upload);
            LOGGER.info("写入文件成功");
            result.setResult("success");
            result.setData("上传成功");
            return result;
        } catch (IllegalStateException e) {
            LOGGER.error("------产生异常-------");
            result.setResult("error");
            result.setData("IllegalStateException");
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("------产生异常-------");
            result.setResult("error");
            result.setData("IOException");
            e.printStackTrace();
        }
        LOGGER.error("------上传失败-------");
        return result;
    }

    /**
     * 多文件上传
     *
     * @param request
     * @return
     */
    @PostMapping("/batch")
    public Result handleFileUpload(HttpServletRequest request) {
        Result result = new Result();
        List<String> results = new ArrayList<>();
        //获取文件列表
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");

        MultipartFile file = null;
        BufferedOutputStream stream = null;

        result.setResult("all_success");
        int fileSize = files.size();
        //循坏写入文件
        for (int i = 0; i < fileSize; i++) {
            file = files.get(i);
            int fileNum = i + 1;
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    //设置文件路径以及名称
                    stream = new BufferedOutputStream(new FileOutputStream(
                            new File(filePath + file.getOriginalFilename())));
                    stream.write(bytes);
                    stream.close();
                    results.add("第"+ fileNum + "个文件上传成功");
                } catch (IOException e) {
                    e.printStackTrace();
                    stream = null;
                    result.setResult("some_error");
                    results.add("第"+ fileNum + "个文件上传失败");
                }
            } else {
                result.setResult("some_error");
                results.add("第"+ fileNum + "个文件为空");
            }
        }
        result.setData(results);
        return result;
    }

    /**
     * 获取文件列表
     *
     * @return
     */
    @GetMapping("/listFiles")
    public Result listFiles() {
        Result result = new Result();
        List<String> list = new ArrayList<>();
        File upload = new File(filePath);
        //获取文件夹下的所有文件
        File[] files = upload.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                //是文件的情况下才添加到list
                if (file.isFile()) {
                    list.add(file.getName());
                }
            }
        }else {
            result.setData(list);
            result.setResult("error");
            return result;
        }
        result.setData(list);
        result.setResult("success");
        return result;
    }

    /**
     * 下载文件
     * @param fileName
     * @param response
     * @return
     */
    @GetMapping("/download/{fileName}")
    public Result download(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        Result result = new Result();
        Path path = Paths.get(filePath, fileName);
        File file = path.toFile();
        if (file.exists() && file.isFile()) {
            response.setContentType("application/force-download");
            try {
                response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            byte[] bytes = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;

            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);

                OutputStream os = response.getOutputStream();
                int len = 0;
                while ((len = bis.read(bytes)) != -1) {
                    os.write(bytes,0,len);
                }

                result.setResult("success");
                result.setData("下载成功");
                return result;

            } catch (Exception e) {

            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        result.setResult("error");
        result.setData("下载失败");
        return result;
    }
}
