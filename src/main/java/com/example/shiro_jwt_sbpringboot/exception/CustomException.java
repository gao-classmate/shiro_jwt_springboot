package com.example.shiro_jwt_sbpringboot.exception;
/**
 * 自定义异常(CustomException)
 * @author dolyw.com
 * @date 2018/8/30 13:59
 */
public class CustomException extends RuntimeException {

    public CustomException(String msg){
        super(msg);
    }

    public CustomException() {
        super();
    }
}
