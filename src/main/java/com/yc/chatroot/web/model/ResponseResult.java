package com.yc.chatroot.web.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // 生成构造器
public class ResponseResult {

    private int code;
    private String msg;
    private Object data;

    public static ResponseResult ok(){
        return ResponseResult.builder().code(1).msg("操作成功").build();
    }

    public static ResponseResult ok(String msg){
        return ResponseResult.builder().code(1).msg(msg).build();
    }

    public static ResponseResult error(){
        return ResponseResult.builder().code(0).msg("操作失败").build();
    }

    public static ResponseResult error(String msg){
        return ResponseResult.builder().code(0).msg(msg).build();
    }

    public <T> ResponseResult setData(T data){
        this.data = data;
        return this;
    }
}
