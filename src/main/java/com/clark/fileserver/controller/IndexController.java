package com.clark.fileserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: ClarkRao
 * @Date: 2018/12/1 16:08
 * @Description: 用于页面跳转
 */
@Controller
public class IndexController {
    @RequestMapping("/oldFile")
    public String oldFile() {
        return "file";
    }

    @RequestMapping("/")
    public String index() {
        return "anotherfile";
    }
}
