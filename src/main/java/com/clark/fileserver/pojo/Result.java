package com.clark.fileserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ClarkRao
 * @Date: 2018/12/1 21:01
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private String result;
    private Object data;
}
